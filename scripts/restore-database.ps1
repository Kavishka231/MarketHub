[CmdletBinding()]
param(
    [Parameter(Mandatory)][string]$BackupPath,
    [Parameter(Mandatory)][ValidateSet("local", "test", "staging")][string]$TargetEnvironment,
    [Parameter(Mandatory)][switch]$ConfirmRestore,
    [string]$DatabaseHost = $(if ($env:DB_HOST) { $env:DB_HOST } else { "localhost" }),
    [int]$DatabasePort = $(if ($env:DB_PORT) { [int]$env:DB_PORT } else { 5432 }),
    [string]$DatabaseName = $env:DB_NAME,
    [string]$DatabaseUser = $env:DB_USERNAME
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
Import-Module (Join-Path $PSScriptRoot "DatabaseBackup.Common.psm1") -Force

if ([string]::IsNullOrWhiteSpace($DatabaseName) -or [string]::IsNullOrWhiteSpace($DatabaseUser)) {
    throw "DB_NAME and DB_USERNAME (or matching parameters) are required."
}

Assert-SafeRestoreTarget `
    -DatabaseName $DatabaseName `
    -TargetEnvironment $TargetEnvironment `
    -ConfirmRestore:$ConfirmRestore
Assert-Executable -Name "pg_restore"
$resolvedBackup = (Resolve-Path -LiteralPath $BackupPath).Path
Assert-Checksum -Path $resolvedBackup | Out-Null

$restoreArguments = @(
    "--host=$DatabaseHost"
    "--port=$DatabasePort"
    "--username=$DatabaseUser"
    "--dbname=$DatabaseName"
    "--clean"
    "--if-exists"
    "--no-owner"
    "--no-privileges"
    "--exit-on-error"
    $resolvedBackup
)
& pg_restore @restoreArguments

if ($LASTEXITCODE -ne 0) {
    throw "pg_restore failed with exit code $LASTEXITCODE."
}

Write-Output "Restore completed after SHA-256 verification: $resolvedBackup"