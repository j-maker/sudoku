# Genera el AAB firmado para Google Play Console.
# Requiere signing.properties en la raíz del proyecto (ver signing.properties.example).

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

if (-not (Test-Path "signing.properties")) {
    Write-Host "ERROR: Crea signing.properties desde signing.properties.example" -ForegroundColor Red
    exit 1
}

Write-Host "Compilando app-release.aab ..."
& .\gradlew.bat bundleRelease
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$aab = "app\build\outputs\bundle\release\app-release.aab"
if (Test-Path $aab) {
    Write-Host "OK: $aab" -ForegroundColor Green
} else {
    Write-Host "No se encontró el AAB en la ruta esperada." -ForegroundColor Yellow
}
