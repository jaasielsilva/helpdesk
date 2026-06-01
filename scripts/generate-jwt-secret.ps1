# Gera JWT_SECRET criptograficamente seguro (256 bits) e grava/substitui no .env
param(
    [string]$EnvFile = ".env"
)

$ErrorActionPreference = "Stop"
$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
$EnvPath = Join-Path $Root $EnvFile
$ExamplePath = Join-Path $Root ".env.example"

if (-not (Test-Path $EnvPath)) {
    if (Test-Path $ExamplePath) {
        Copy-Item $ExamplePath $EnvPath
        Write-Host "Arquivo $EnvFile criado a partir de .env.example"
    } else {
        New-Item -Path $EnvPath -ItemType File -Force | Out-Null
        Write-Host "Arquivo $EnvFile criado"
    }
}

$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$secret = [Convert]::ToBase64String($bytes)

$lines = @()
if (Test-Path $EnvPath) {
    $lines = Get-Content $EnvPath | Where-Object { $_ -notmatch '^\s*JWT_SECRET=' }
}

$lines += "JWT_SECRET=$secret"
Set-Content -Path $EnvPath -Value $lines -Encoding utf8

Write-Host ""
Write-Host "JWT_SECRET gerado com sucesso (256 bits / 32 bytes)"
Write-Host "Arquivo: $EnvPath"
Write-Host "Valor anterior substituido."
Write-Host ""
Write-Host "Producao: reinicie o backend com profile prod apos gerar o secret."
Write-Host "  make jwt-secret   ou   .\scripts\generate-jwt-secret.ps1"
Write-Host ""
