<#
.SYNOPSIS
  Compila el AAB y lo copia a store-assets/release/<version>/ con nombre versionado.
  Lee versionCode y versionName desde app.template.properties.
#>
$ErrorActionPreference = "Stop"
$Root = Split-Path $PSScriptRoot -Parent
$PropsFile = Join-Path $Root "app.template.properties"

if (-not (Test-Path $PropsFile)) {
    Write-Host "No se encontro app.template.properties" -ForegroundColor Red
    exit 1
}

$propsText = Get-Content $PropsFile -Raw
if ($propsText -notmatch '(?m)^versionCode\s*=\s*(\d+)\s*$') {
    Write-Host "No se pudo leer versionCode en app.template.properties" -ForegroundColor Red
    exit 1
}
$versionCode = [int]$Matches[1]

if ($propsText -notmatch '(?m)^versionName\s*=\s*([^\s#]+)\s*$') {
    Write-Host "No se pudo leer versionName en app.template.properties" -ForegroundColor Red
    exit 1
}
$versionName = $Matches[1]

$releaseDir = Join-Path $Root "store-assets\release\$versionName"
$aabName = "sudoku-$versionName-v$versionCode.aab"
$destAab = Join-Path $releaseDir $aabName
$destSymbols = Join-Path $releaseDir "native-debug-symbols.zip"

& (Join-Path $Root "scripts\build-release-aab.ps1")
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$srcAab = Join-Path $Root "app\build\outputs\bundle\release\app-release.aab"
$srcSymbols = Join-Path $Root "app\build\outputs\native-debug-symbols\release\native-debug-symbols.zip"

New-Item -ItemType Directory -Force -Path $releaseDir | Out-Null
Copy-Item -Force $srcAab $destAab
if (Test-Path $srcSymbols) {
    Copy-Item -Force $srcSymbols $destSymbols
}

Write-Host ""
Write-Host "Release preparada en store-assets:" -ForegroundColor Green
Write-Host "  $destAab"
if (Test-Path $destSymbols) {
    Write-Host "  $destSymbols"
}
Write-Host ""
Write-Host "Notas: store-assets\release-notes-$versionName.txt"
Write-Host "Guia:  store-assets\RELEASE-$versionName.md"
