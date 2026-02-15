# Squash and Push

Utility script that squashes all commits since the merge-base with `origin/develop` into a single commit and pushes.

## Usage

```powershell
.\run-push.ps1
```

## What It Does

1. **Fetches** all remotes and prunes stale references (`git fetch --all --prune`)
2. **Checks** that the working tree is clean (no uncommitted changes)
3. **Finds** the merge-base between `HEAD` and `origin/develop`
4. **Counts** commits since the merge-base — exits silently if there are none
5. **Prompts** for confirmation (`Proceed? [y/N]`)
6. **Prompts** for a commit message
7. **Squashes** by soft-resetting to the merge-base (`git reset --soft <merge-base>`)
8. **Commits** with the provided message
9. **Pushes** to the remote

## Interactive Prompts

The script requires two interactive inputs:

| Prompt | Input | Default |
|--------|-------|---------|
| `Proceed? [y/N]` | `y` or `n` | No (abort) |
| `Message` | Commit message text | Required (cannot be empty) |

## Exit Codes

| Code | Meaning |
|------|---------|
| `0` | Push completed successfully, or no commits to squash |
| `1` | Error (dirty working tree, no merge-base found, or Git operation failed) |

## Prerequisites

- Git
- Clean working tree (no uncommitted changes)
- A branch that shares history with `origin/develop`
