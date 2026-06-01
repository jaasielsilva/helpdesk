# Build release JAR and copy to release-artifacts/v<version>/
param(
    [switch]$SkipTests
)

$ErrorActionPreference = "Stop"
$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $Root

$pom = [xml](Get-Content "pom.xml")
$version = $pom.project.version
$jarName = "helpdesk-$version.jar"
$outDir = Join-Path $Root "release-artifacts\v$version"

Write-Host "Building Helpdesk Pro v$version ..."

$testArg = if ($SkipTests) { "-DskipTests" } else { "" }
Invoke-Expression "mvn -q clean package $testArg"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$builtJar = Join-Path $Root "target\$jarName"
if (-not (Test-Path $builtJar)) {
    Write-Error "JAR not found: $builtJar"
}

New-Item -ItemType Directory -Force -Path $outDir | Out-Null
Copy-Item -Force $builtJar (Join-Path $outDir $jarName)

$hash = (Get-FileHash (Join-Path $outDir $jarName) -Algorithm SHA256).Hash.ToLower()
Set-Content -Path (Join-Path $outDir "$jarName.sha256") -Value "$hash  $jarName" -Encoding UTF8

Write-Host ""
Write-Host "Release artifact ready:"
Write-Host "  $outDir\$jarName"
Write-Host "  $outDir\$jarName.sha256"
Write-Host ""
Write-Host "Verify: Get-FileHash $outDir\$jarName -Algorithm SHA256"
