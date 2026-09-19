param(
    [string]$BackendUrl = "http://localhost:8080",
    [string]$FrontendUrl = "http://localhost:3000",
    [switch]$VerifyRedis
)

$ErrorActionPreference = "Stop"
if ((Invoke-RestMethod "$BackendUrl/actuator/health").status -ne "UP") { throw "Backend health is not UP" }
if ((Invoke-RestMethod "$BackendUrl/api/readiness").status -ne "UP") { throw "Backend readiness is not UP" }
if ((Invoke-WebRequest "$FrontendUrl" -UseBasicParsing).StatusCode -ne 200) { throw "Frontend is unavailable" }
$products = Invoke-WebRequest "$BackendUrl/api/products?page=0&size=1" -UseBasicParsing
if ($products.StatusCode -ne 200) { throw "Public product endpoint is unavailable" }
try {
    Invoke-RestMethod "$BackendUrl/api/cart"
    throw "Protected cart endpoint allowed anonymous access"
} catch {
    if ($_.Exception.Response.StatusCode.value__ -ne 401) { throw }
}
if ($VerifyRedis) {
    $pong = docker compose exec -T redis redis-cli ping
    if ($pong.Trim() -ne "PONG") { throw "Redis did not respond to PING" }
}
Write-Host "MarketHub smoke checks passed."