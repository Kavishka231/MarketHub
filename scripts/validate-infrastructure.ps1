$ErrorActionPreference = "Stop"
Get-Content "monitoring/grafana/dashboards/markethub-overview.json" -Raw | ConvertFrom-Json | Out-Null
$required = @(
    "monitoring/prometheus/prometheus.yml",
    "monitoring/prometheus/rules/markethub-alerts.yml",
    "monitoring/grafana/provisioning/datasources/prometheus.yml",
    "monitoring/grafana/provisioning/dashboards/dashboards.yml"
)
foreach ($file in $required) {
    if (-not (Test-Path $file)) { throw "Missing infrastructure file: $file" }
}
Write-Host "Infrastructure files passed structural validation."