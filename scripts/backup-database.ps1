[CmdletBinding()]
param(
    [string]$BackupDirectory = (Join-Path $PSScriptRoot "..\backups"),
    [ValidateRange(1, 365)][int]$RetainCount = 7,
    [string]$DatabaseHost = $(if ($env:DB_HOST) { $env:DB_HOST } else { "localhost" }),
    [int]$DatabasePort = $(if ($env:DB_PORT) { [int]$env:DB_PORT } else { 5432 }),
    [string]$DatabaseName = $env:DB_NAME,
    [string]$DatabaseUser = $env:DB_USERNAME,
    [string]$GitSha = $(if ($env:GIT_SHA) { $env:GIT_SHA } else { "unknown" })
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
Import-Module (Join-Path $PSScriptRoot "DatabaseBackup.Common.psm1") -Force

if ([string]::IsNullOrWhiteSpace($DatabaseName) -or [string]::IsNullOrWhiteSpace($DatabaseUser)) {
    throw "DB_NAME and DB_USERNAME (or matching parameters) are required."
}

Assert-Executable -Name "pg_dump"
New-Item -ItemType Directory -Path $BackupDirectory -Force | Out-Null
$resolvedDirectory = (Resolve-Path -LiteralPath $BackupDirectory).Path
$timestamp = (Get-Date).ToUniversalTime().ToString("yyyyMMdd-HHmmss")
$backupPath = Join-Path $resolvedDirectory "markethub-$timestamp.dump"

$dumpArguments = @(
    "--host=$DatabaseHost"
    "--port=$DatabasePort"
    "--username=$DatabaseUser"
    "--dbname=$DatabaseName"
    "--format=custom"
    "--compress=9"
    "--no-owner"
    "--no-privileges"
    "--file=$backupPath"
)
& pg_dump @dumpArguments

if ($LASTEXITCODE -ne 0) {
    throw "pg_dump failed with exit code $LASTEXITCODE."
}

if (-not (Test-Path -LiteralPath $backupPath -PathType Leaf) -or (Get-Item -LiteralPath $backupPath).Length -eq 0) {
    throw "pg_dump did not produce a non-empty backup file."
}

$checksumPath = "$backupPath.sha256"
$checksum = Write-ChecksumFile -Path $backupPath -ChecksumPath $checksumPath
$postgresVersion = (& pg_dump --version) -join " "
$manifest = [ordered]@{
    createdAt = (Get-Date).ToUniversalTime().ToString("o")
    database = $DatabaseName
    gitSha = $GitSha
    dumpFormat = "postgresql-custom"
    postgresToolVersion = $postgresVersion
    file = [System.IO.Path]::GetFileName($backupPath)
    sha256 = $checksum
}
$manifest | ConvertTo-Json | Set-Content -LiteralPath "$backupPath.manifest.json" -Encoding utf8

Invoke-BackupRetention -Directory $resolvedDirectory -RetainCount $RetainCount

[pscustomobject]@{
    BackupPath = $backupPath
    ChecksumPath = $checksumPath
    ManifestPath = "$backupPath.manifest.json"
    Sha256 = $checksum
}