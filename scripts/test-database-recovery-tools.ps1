Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
Import-Module (Join-Path $PSScriptRoot "DatabaseBackup.Common.psm1") -Force

$testDirectory = Join-Path ([System.IO.Path]::GetTempPath()) "markethub-recovery-tests-$([Guid]::NewGuid())"
New-Item -ItemType Directory -Path $testDirectory | Out-Null

try {
    $backup = Join-Path $testDirectory "markethub-20260919-180000.dump"
    [System.IO.File]::WriteAllText($backup, "verified test backup")
    Write-ChecksumFile -Path $backup -ChecksumPath "$backup.sha256" | Out-Null
    Assert-Checksum -Path $backup | Out-Null

    [System.IO.File]::WriteAllText($backup, "tampered backup")
    try {
        Assert-Checksum -Path $backup | Out-Null
        throw "Checksum mismatch was not rejected."
    } catch {
        if ($_.Exception.Message -eq "Checksum mismatch was not rejected.") { throw }
    }

    try {
        Assert-SafeRestoreTarget -DatabaseName "markethub_production" -TargetEnvironment staging -ConfirmRestore
        throw "Production-like restore target was not rejected."
    } catch {
        if ($_.Exception.Message -eq "Production-like restore target was not rejected.") { throw }
    }
    try {
        Assert-SafeRestoreTarget -DatabaseName "markethub_test" -TargetEnvironment test
        throw "Restore without confirmation was not rejected."
    } catch {
        if ($_.Exception.Message -eq "Restore without confirmation was not rejected.") { throw }
    }

    try {
        Assert-Checksum -Path (Join-Path $testDirectory "missing.dump") | Out-Null
        throw "Missing backup was not rejected."
    } catch {
        if ($_.Exception.Message -eq "Missing backup was not rejected.") { throw }
    }

    1..3 | ForEach-Object {
        $path = Join-Path $testDirectory "markethub-20260919-18000$_.dump"
        [System.IO.File]::WriteAllText($path, "backup $_")
        (Get-Item $path).LastWriteTimeUtc = [DateTime]::UtcNow.AddMinutes(-$_)
    }
    Invoke-BackupRetention -Directory $testDirectory -RetainCount 2
    $remaining = @(Get-ChildItem -LiteralPath $testDirectory -Filter "markethub-*.dump")
    if ($remaining.Count -ne 2) {
        throw "Retention did not preserve exactly two newest backups."
    }

    Write-Output "Recovery tooling safety tests passed."
} finally {
    Remove-Item -LiteralPath $testDirectory -Recurse -Force
}