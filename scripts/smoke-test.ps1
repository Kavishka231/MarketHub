param(
    [string]$BackendUrl = "http://localhost:8080",
    [string]$FrontendUrl = "http://localhost:3000",
    [string]$ExpectedGitSha,
    [switch]$VerifyRedis
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if ((Invoke-RestMethod "$BackendUrl/actuator/health").status -ne "UP") {
    throw "Backend health is not UP"
}
if ((Invoke-RestMethod "$BackendUrl/api/readiness").status -ne "UP") {
    throw "Backend readiness is not UP"
}
if ((Invoke-WebRequest "$FrontendUrl" -UseBasicParsing).StatusCode -ne 200) {
    throw "Frontend is unavailable"
}
if ((Invoke-WebRequest "$BackendUrl/api/products?page=0&size=1" -UseBasicParsing).StatusCode -ne 200) {
    throw "Public product endpoint is unavailable"
}

$buildInfo = Invoke-RestMethod "$BackendUrl/actuator/info"
if ([string]::IsNullOrWhiteSpace($buildInfo.app.version) -or [string]::IsNullOrWhiteSpace($buildInfo.app.gitSha)) {
    throw "Build metadata is incomplete"
}
if ($ExpectedGitSha -and $buildInfo.app.gitSha -ne $ExpectedGitSha) {
    throw "Running Git SHA '$($buildInfo.app.gitSha)' does not match intended SHA '$ExpectedGitSha'"
}

try {
    Invoke-RestMethod "$BackendUrl/api/cart"
    throw "Protected cart endpoint allowed anonymous access"
} catch {
    if ($_.Exception.Response.StatusCode.value__ -ne 401) { throw }
}

if ($VerifyRedis) {
    $pong = docker compose exec -T redis redis-cli ping
    if ($LASTEXITCODE -ne 0 -or $pong.Trim() -ne "PONG") {
        throw "Redis did not respond to PING"
    }
}

Write-Host "MarketHub smoke checks passed for $($buildInfo.app.version) at $($buildInfo.app.gitSha)."
