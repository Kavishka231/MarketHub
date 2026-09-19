param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern("^[0-9a-f]{7,40}$")]
    [string]$ImageTag,
    [Parameter(Mandatory = $true)]
    [string]$ManifestPath,
    [string]$ComposeFile = "docker-compose.yml",
    [switch]$DryRun
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
Import-Module (Join-Path $PSScriptRoot "DatabaseBackup.Common.psm1") -Force

$resolvedManifest = (Resolve-Path -LiteralPath $ManifestPath).Path
Assert-Checksum -Path $resolvedManifest | Out-Null
$manifest = Get-Content -LiteralPath $resolvedManifest -Raw | ConvertFrom-Json
if ($manifest.gitSha -ne $ImageTag -or $manifest.imageTag -ne $ImageTag) {
    throw "Release manifest SHA/image tag does not match the intended deployment."
}

$stateFile = ".deployment-current"
$previousTag = if (Test-Path $stateFile) {
    (Get-Content $stateFile -Raw).Trim()
} else {
    $null
}

if ($DryRun) {
    Write-Host "Would deploy verified immutable image tag $ImageTag using $ComposeFile"
    Write-Host "Migration target: $($manifest.migrationVersion)"
    Write-Host "Rollback tag: $(if ($previousTag) { $previousTag } else { '<none>' })"
    exit 0
}

$env:MARKETHUB_IMAGE_TAG = $ImageTag
$env:BUILD_TIME = $manifest.buildTimestamp
try {
    docker compose -f $ComposeFile build backend frontend
    if ($LASTEXITCODE -ne 0) { throw "Image build failed" }

    docker compose -f $ComposeFile up -d --wait
    if ($LASTEXITCODE -ne 0) { throw "Compose startup or Flyway migration failed" }

    $migration = docker compose -f $ComposeFile exec -T database `
        psql -U markethub -d markethub -tAc `
        "SELECT version FROM flyway_schema_history WHERE success = true ORDER BY installed_rank DESC LIMIT 1;"
    if ($LASTEXITCODE -ne 0 -or $migration.Trim() -ne $manifest.migrationVersion) {
        throw "Running Flyway version does not match the release manifest."
    }

    & "$PSScriptRoot/smoke-test.ps1" -ExpectedGitSha $ImageTag
    if ($LASTEXITCODE -ne 0) { throw "Smoke verification failed" }

    [System.IO.File]::WriteAllText((Join-Path $PWD $stateFile), "$ImageTag`n")
    Write-Host "Staging deployment $ImageTag verified against build info and migration state."
} catch {
    if ($previousTag) {
        Write-Warning "Deployment failed; restoring image $previousTag. Flyway migrations are forward-only and must remain backward-compatible."
        $env:MARKETHUB_IMAGE_TAG = $previousTag
        docker compose -f $ComposeFile up -d --no-build --wait
        if ($LASTEXITCODE -ne 0) { throw "Rollback startup failed" }
        & "$PSScriptRoot/smoke-test.ps1" -ExpectedGitSha $previousTag
    }
    throw
}
