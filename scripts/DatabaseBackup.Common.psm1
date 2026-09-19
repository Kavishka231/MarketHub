Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Assert-Executable {
    param([Parameter(Mandatory)][string]$Name)

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Required executable '$Name' was not found on PATH."
    }
}

function Get-Sha256 {
    param([Parameter(Mandatory)][string]$Path)

    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        throw "File does not exist: $Path"
    }

    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
}

function Write-ChecksumFile {
    param(
        [Parameter(Mandatory)][string]$Path,
        [Parameter(Mandatory)][string]$ChecksumPath
    )

    $hash = Get-Sha256 -Path $Path
    $line = "$hash  $([System.IO.Path]::GetFileName($Path))`n"
    [System.IO.File]::WriteAllText($ChecksumPath, $line, [System.Text.UTF8Encoding]::new($false))
    return $hash
}

function Assert-Checksum {
    param(
        [Parameter(Mandatory)][string]$Path,
        [string]$ChecksumPath = "$Path.sha256"
    )

    if (-not (Test-Path -LiteralPath $ChecksumPath -PathType Leaf)) {
        throw "Checksum file does not exist: $ChecksumPath"
    }

    $expected = ((Get-Content -LiteralPath $ChecksumPath -Raw).Trim() -split '\s+')[0].ToLowerInvariant()
    $actual = Get-Sha256 -Path $Path

    if ($expected -notmatch '^[a-f0-9]{64}$' -or $actual -ne $expected) {
        throw "SHA-256 verification failed for $Path"
    }

    return $actual
}

function Assert-SafeRestoreTarget {
    param(
        [Parameter(Mandatory)][string]$DatabaseName,
        [Parameter(Mandatory)][ValidateSet("local", "test", "staging")][string]$TargetEnvironment,
        [switch]$ConfirmRestore
    )

    if (-not $ConfirmRestore) {
        throw "Restore requires the explicit -ConfirmRestore switch."
    }

    if ($DatabaseName -match '(?i)(^|[-_])(prod|production)([-_]|$)') {
        throw "Production-like database names are refused by this restore tool."
    }

    if ($TargetEnvironment -notin @("local", "test", "staging")) {
        throw "Unsupported restore environment: $TargetEnvironment"
    }
}

function Invoke-BackupRetention {
    param(
        [Parameter(Mandatory)][string]$Directory,
        [ValidateRange(1, 365)][int]$RetainCount
    )

    $resolvedDirectory = (Resolve-Path -LiteralPath $Directory).Path
    $backups = @(Get-ChildItem -LiteralPath $resolvedDirectory -File -Filter "markethub-*.dump" |
        Sort-Object LastWriteTimeUtc -Descending)

    foreach ($backup in ($backups | Select-Object -Skip $RetainCount)) {
        if (-not $backup.FullName.StartsWith($resolvedDirectory, [StringComparison]::OrdinalIgnoreCase)) {
            throw "Retention candidate escaped the configured backup directory."
        }

        foreach ($candidate in @($backup.FullName, "$($backup.FullName).sha256", "$($backup.FullName).manifest.json")) {
            if (Test-Path -LiteralPath $candidate -PathType Leaf) {
                Remove-Item -LiteralPath $candidate -Force
            }
        }
    }
}

Export-ModuleMember -Function Assert-Executable, Get-Sha256, Write-ChecksumFile, Assert-Checksum, Assert-SafeRestoreTarget, Invoke-BackupRetention