Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
Import-Module (Join-Path $PSScriptRoot "DatabaseBackup.Common.psm1") -Force

$testDirectory = Join-Path ([System.IO.Path]::GetTempPath()) "markethub-release-tests-$([Guid]::NewGuid())"
New-Item -ItemType Directory -Path $testDirectory | Out-Null

try {
    $jar = Join-Path $testDirectory "markethub-api.jar"
    $frontend = Join-Path $testDirectory "frontend.tar.gz"
    $backendSbom = Join-Path $testDirectory "backend-sbom.json"
    $frontendSbom = Join-Path $testDirectory "frontend-sbom.json"
    [System.IO.File]::WriteAllText($jar, "test jar")
    [System.IO.File]::WriteAllText($frontend, "test frontend")
    [System.IO.File]::WriteAllText($backendSbom, '{"bomFormat":"CycloneDX"}')
    [System.IO.File]::WriteAllText($frontendSbom, '{"bomFormat":"CycloneDX"}')

    $result = & (Join-Path $PSScriptRoot "release.ps1") `
        -GitSha "abcdef1234567890" `
        -ArtifactPath @($jar, $frontend) `
        -SbomPath @($backendSbom, $frontendSbom) `
        -OutputDirectory (Join-Path $testDirectory "release")

    Assert-Checksum -Path $result.ManifestPath | Out-Null
    $manifest = Get-Content -LiteralPath $result.ManifestPath -Raw | ConvertFrom-Json
    if ($manifest.gitSha -ne "abcdef1234567890" -or $manifest.migrationVersion -ne "11") {
        throw "Release manifest did not preserve the intended SHA or migration version."
    }
    if ($manifest.artifacts.Count -ne 2 -or $manifest.sboms.Count -ne 2) {
        throw "Release manifest omitted required artifacts or SBOMs."
    }

    [System.IO.File]::AppendAllText($result.ManifestPath, "tampered")
    try {
        Assert-Checksum -Path $result.ManifestPath | Out-Null
        throw "Tampered release manifest was not rejected."
    } catch {
        if ($_.Exception.Message -eq "Tampered release manifest was not rejected.") { throw }
    }

    try {
        & (Join-Path $PSScriptRoot "release.ps1") `
            -GitSha "abcdef1" `
            -ImageTag "mutable-latest" `
            -ArtifactPath $jar `
            -SbomPath $backendSbom `
            -OutputDirectory (Join-Path $testDirectory "invalid")
        throw "Mutable or mismatched image tag was not rejected."
    } catch {
        if ($_.Exception.Message -eq "Mutable or mismatched image tag was not rejected.") { throw }
    }

    Write-Output "Release tooling integrity tests passed."
} finally {
    Remove-Item -LiteralPath $testDirectory -Recurse -Force
}
