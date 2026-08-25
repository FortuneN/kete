# =============================================================================
#
#   KETE │ Shaded JAR Check
#
#   Verifies that the shaded target/kete.jar only ships classes under the
#   relocation roots (kete/ and io/github/fortunen/), so that nothing in it
#   can collide with Keycloak's own classpath.
#
#   Usage: .\run-jar-check.ps1 [-JarPath target/kete.jar]
#
# =============================================================================

param([string]$JarPath = "target/kete.jar")

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if (-not (Test-Path $JarPath)) {
    Write-Host "  ✗ $JarPath not found (run 'mvn package -DskipTests' first)" -ForegroundColor Red
    exit 1
}

Add-Type -AssemblyName System.IO.Compression.FileSystem

$allowedRoots = @("kete/", "io/github/fortunen/")
$offenders = @{}
$classCount = 0

$archive = [System.IO.Compression.ZipFile]::OpenRead((Resolve-Path $JarPath).Path)

try {
    foreach ($entry in $archive.Entries) {
        $name = $entry.FullName
        if (-not $name.EndsWith(".class")) { continue }
        $classCount++
        $allowed = $false
        foreach ($root in $allowedRoots) {
            if ($name.StartsWith($root)) { $allowed = $true; break }
        }
        if ($allowed) { continue }
        $segments = $name.Split("/")
        $rootKey = if ($segments.Count -gt 2) { "$($segments[0])/$($segments[1])" } else { $segments[0] }
        if ($offenders.ContainsKey($rootKey)) { $offenders[$rootKey]++ } else { $offenders[$rootKey] = 1 }
    }
} finally {
    $archive.Dispose()
}

if ($offenders.Count -eq 0) {
    Write-Host "  ✓ $classCount classes, all under $($allowedRoots -join ', ')" -ForegroundColor Green
    exit 0
}

Write-Host "  ✗ Classes outside the relocation roots:" -ForegroundColor Red

foreach ($key in ($offenders.Keys | Sort-Object)) {
    Write-Host ("      {0,6}  {1}" -f $offenders[$key], $key) -ForegroundColor Red
}

Write-Host "  Add a <relocation> for each package to the maven-shade-plugin configuration in pom.xml" -ForegroundColor DarkGray
exit 1
