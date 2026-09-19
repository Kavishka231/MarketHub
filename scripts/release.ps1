[CmdletBinding()]
param(
    [Parameter(Mandatory)][ValidatePattern("^[0-9a-f]{7,40}$")][string]$GitSha,
    [Parameter(Mandatory)][string[]]$ArtifactPath,
    [Parameter(Mandatory)][string[]]$SbomPath,
    [string]$OutputDirectory = (Join-Path $PSScriptRoot "..\artifacts\release"),
    [string]$ImageTag = $GitSha,
    [string]$ApplicationVersion = "0.0.1-SNAPSHOT",
    [switch]$SmokeVerified
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
Import-Module (Join-Path $PSScriptRoot "DatabaseBackup.Common.psm1") -Force

if ($ImageTag -ne $GitSha) {
    throw "The immutable image tag must equal the intended Git SHA."
}

New-Item -ItemType Directory -Path $OutputDirectory -Force | Out-Null
$resolvedOutput = (Resolve-Path -LiteralPath $OutputDirectory).Path
$seenNames = @{}

function Copy-ReleaseFile {
    param(
        [Parameter(Mandatory)][string]$Path,
        [Parameter(Mandatory)][string]$Kind
    )

    $source = (Resolve-Path -LiteralPath $Path).Path
    $name = [System.IO.Path]::GetFileName($source)
    if ($seenNames.ContainsKey($name)) {
        throw "Release inputs contain duplicate file name: $name"
    }
    $seenNames[$name] = $true

    $destination = Join-Path $resolvedOutput $name
    Copy-Item -LiteralPath $source -Destination $destination -Force
    $checksumPath = "$destination.sha256"
    $checksum = Write-ChecksumFile -Path $destination -ChecksumPath $checksumPath

    return [ordered]@{
        kind = $Kind
        file = $name
        sha256 = $checksum
    }
}

$artifactRecords = @($ArtifactPath | ForEach-Object { Copy-ReleaseFile -Path $_ -Kind "artifact" })
$sbomRecords = @($SbomPath | ForEach-Object { Copy-ReleaseFile -Path $_ -Kind "sbom" })
$migrationFiles = @(Get-ChildItem (Join-Path $PSScriptRoot "..\backend\src\main\resources\db\migration") -Filter "V*__*.sql")
$migrationVersions = @($migrationFiles | ForEach-Object {
    if ($_.BaseName -match '^V([0-9]+)__') { [int]$Matches[1] }
})
if ($migrationVersions.Count -eq 0) {
    throw "No Flyway migrations were found."
}

$manifest = [ordered]@{
    schemaVersion = 1
    application = "MarketHub"
    applicationVersion = $ApplicationVersion
    gitSha = $GitSha
    imageTag = $ImageTag
    buildTimestamp = (Get-Date).ToUniversalTime().ToString("o")
    migrationVersion = ($migrationVersions | Measure-Object -Maximum).Maximum.ToString()
    smokeVerified = [bool]$SmokeVerified
    artifacts = $artifactRecords
    sboms = $sbomRecords
}
$manifestPath = Join-Path $resolvedOutput "release-manifest.json"
$manifest | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath $manifestPath -Encoding utf8
$manifestChecksum = Write-ChecksumFile -Path $manifestPath -ChecksumPath "$manifestPath.sha256"

Write-Output ([pscustomobject]@{
    ManifestPath = $manifestPath
    ManifestChecksum = $manifestChecksum
    MigrationVersion = $manifest.migrationVersion
})
