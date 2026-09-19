[CmdletBinding()]
param(
    [string]$SourceDatabase = "markethub_source",
    [string]$RecoveryDatabase = "markethub_recovery",
    [string]$DatabaseHost = "localhost",
    [int]$DatabasePort = 5432,
    [string]$DatabaseUser = "postgres",
    [string]$BackupDirectory = (Join-Path $PSScriptRoot "..\backups\recovery-verification")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
Import-Module (Join-Path $PSScriptRoot "DatabaseBackup.Common.psm1") -Force
Assert-Executable -Name "psql"

$backupParameters = @{
    BackupDirectory = $BackupDirectory
    RetainCount = 2
    DatabaseHost = $DatabaseHost
    DatabasePort = $DatabasePort
    DatabaseName = $SourceDatabase
    DatabaseUser = $DatabaseUser
}
$backup = & (Join-Path $PSScriptRoot "backup-database.ps1") @backupParameters

$restoreParameters = @{
    BackupPath = $backup.BackupPath
    TargetEnvironment = "test"
    ConfirmRestore = $true
    DatabaseHost = $DatabaseHost
    DatabasePort = $DatabasePort
    DatabaseName = $RecoveryDatabase
    DatabaseUser = $DatabaseUser
}
& (Join-Path $PSScriptRoot "restore-database.ps1") @restoreParameters
$sourceUsers = (& psql --host=$DatabaseHost --port=$DatabasePort --username=$DatabaseUser --dbname=$SourceDatabase --tuples-only --no-align --command="SELECT COUNT(*) FROM users;").Trim()
$recoveredUsers = (& psql --host=$DatabaseHost --port=$DatabasePort --username=$DatabaseUser --dbname=$RecoveryDatabase --tuples-only --no-align --command="SELECT COUNT(*) FROM users;").Trim()
$migrationVersion = (& psql --host=$DatabaseHost --port=$DatabasePort --username=$DatabaseUser --dbname=$RecoveryDatabase --tuples-only --no-align --command="SELECT version FROM flyway_schema_history WHERE success = true ORDER BY installed_rank DESC LIMIT 1;").Trim()

if ($LASTEXITCODE -ne 0 -or $sourceUsers -ne $recoveredUsers -or [string]::IsNullOrWhiteSpace($migrationVersion)) {
    throw "Recovery verification failed: user counts or migration state differ."
}

Write-Output "Recovery verified: users=$recoveredUsers migration=$migrationVersion checksum=$($backup.Sha256)"