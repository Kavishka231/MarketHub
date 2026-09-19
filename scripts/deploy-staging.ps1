param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern("^[0-9a-f]{7,40}$")]
    [string]$ImageTag,
    [string]$ComposeFile = "docker-compose.yml",
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"
$stateFile = ".deployment-current"
$previousTag = if (Test-Path $stateFile) { (Get-Content $stateFile -Raw).Trim() } else { $null }

if ($DryRun) {
    Write-Host "Would deploy immutable image tag $ImageTag using $ComposeFile"
    Write-Host "Rollback tag: $(if ($previousTag) { $previousTag } else { '<none>' })"
    exit 0
}

$env:MARKETHUB_IMAGE_TAG = $ImageTag
try {
    docker compose -f $ComposeFile build backend frontend
    if ($LASTEXITCODE -ne 0) { throw "Image build failed" }
    docker compose -f $ComposeFile up -d --wait
    if ($LASTEXITCODE -ne 0) { throw "Compose startup failed" }
    & "$PSScriptRoot/smoke-test.ps1"
    if ($LASTEXITCODE -ne 0) { throw "Smoke verification failed" }
    [IO.File]::WriteAllText((Join-Path $PWD $stateFile), "$ImageTag`n")
    Write-Host "Staging deployment $ImageTag verified."
} catch {
    if ($previousTag) {
        Write-Warning "Deployment failed; restoring $previousTag"
        $env:MARKETHUB_IMAGE_TAG = $previousTag
        docker compose -f $ComposeFile up -d --no-build --wait
        & "$PSScriptRoot/smoke-test.ps1"
    }
    throw
}