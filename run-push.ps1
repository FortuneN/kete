# push.ps1

$ErrorActionPreference = 'Stop'

function Read-YesNo([string]$Prompt)
{
	while ($true)
	{
		$raw = Read-Host "$Prompt [y/N]"
		$raw = ($raw ?? '').Trim()

		if ([string]::IsNullOrWhiteSpace($raw))
		{
			return $false
		}

		switch ($raw.ToLowerInvariant())
		{
			{ $_ -in @('y', 'yes') } { return $true }
			{ $_ -in @('n', 'no')  } { return $false }
			default { Write-Host "Please answer y or n." }
		}
	}
}

function Read-NonEmpty([string]$Prompt)
{
	while ($true)
	{
		$value = (Read-Host $Prompt)
		$value = ($value ?? '').Trim()

		if (-not [string]::IsNullOrWhiteSpace($value))
		{
			return $value
		}

		Write-Host "Value cannot be empty."
	}
}

try
{
	git fetch --all --prune | Out-Host

	$status = (git status --porcelain)

	if (-not [string]::IsNullOrWhiteSpace($status))
	{
		git status --porcelain | Out-Host
		throw "Working tree is not clean."
	}

	$mergeBase = (git merge-base HEAD origin/develop).Trim()

	if ([string]::IsNullOrWhiteSpace($mergeBase))
	{
		throw "Could not determine merge-base with origin/develop."
	}

	$commitCount = [int](git rev-list --count "$mergeBase..HEAD")
    
	if ($commitCount -le 0)
	{
		exit 0
	}

	if (-not (Read-YesNo "Proceed?"))
	{
		exit 0
	}

	$msg = Read-NonEmpty "Message"

	git reset --soft $mergeBase | Out-Host
	git commit -m $msg | Out-Host
}
catch
{
	Write-Host "ERROR: $($_.Exception.Message)"
	exit 1
}
