# Documentation Preview

Starts a local MkDocs development server for previewing the documentation site.

## Usage

```powershell
.\run-docs-preview.ps1
```

## What It Does

1. **Stops** any existing Python processes (to free the MkDocs port)
2. **Starts** the MkDocs development server (`python -m mkdocs serve`)

The server runs at `http://127.0.0.1:8000` by default and supports live reload — changes to documentation files are reflected automatically in the browser.

## Prerequisites

- Python
- MkDocs and all documentation plugins installed (see `mkdocs.yml` for required plugins)
