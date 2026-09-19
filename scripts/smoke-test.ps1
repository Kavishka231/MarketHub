param(
    [string]$BackendUrl = "http://localhost:8080",
    [string]$FrontendUrl = "http://localhost:3000"
)

$ErrorActionPreference = "Stop"

$health = Invoke-RestMethod "$BackendUrl/actuator/health"
if ($health.status -ne "UP") {
    throw "Backend health is not UP"
}

$readiness = Invoke-RestMethod "$BackendUrl/api/readiness"
if ($readiness.status -ne "UP") {
    throw "Backend readiness is not UP"
}

$frontend = Invoke-WebRequest $FrontendUrl -UseBasicParsing
if ($frontend.StatusCode -ne 200) {
    throw "Frontend did not return HTTP 200"
}

try {
    Invoke-RestMethod "$BackendUrl/api/cart"
    throw "Protected cart endpoint unexpectedly allowed anonymous access"
} catch {
    if ($_.Exception.Response.StatusCode.value__ -ne 401) {
        throw
    }
}

Write-Host "MarketHub smoke checks passed."