# Imprime SHA-1 para pegar en Firebase -> Huella digital del certificado.
# Uso: .\scripts\print-firebase-sha1.ps1

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
Set-Location $root

Write-Host ""
Write-Host "=== SHA-1 DEBUG (installDebug en tu PC) ===" -ForegroundColor Cyan
$debugKeystore = Join-Path $env:USERPROFILE ".android\debug.keystore"
if (-not (Test-Path $debugKeystore)) {
    Write-Host "No encontrado: $debugKeystore" -ForegroundColor Yellow
} else {
    keytool -list -v -keystore $debugKeystore -alias androiddebugkey -storepass android -keypass android 2>&1 |
        Select-String "SHA1:" |
        ForEach-Object { $_.Line.Trim() }
}

Write-Host ""
Write-Host "=== SHA-1 RELEASE (AAB / Play Store) ===" -ForegroundColor Cyan
$signingFile = Join-Path $root "signing.properties"
if (-not (Test-Path $signingFile)) {
    Write-Host "Crea signing.properties desde signing.properties.example" -ForegroundColor Yellow
} else {
    $props = @{}
    Get-Content $signingFile | ForEach-Object {
        if ($_ -match "^\s*([^#=]+)=(.*)$") { $props[$matches[1].Trim()] = $matches[2].Trim() }
    }
    $storeFile = $props["storeFile"]
    $alias = $props["keyAlias"]
    if (-not $storeFile -or -not $alias) {
        Write-Host "signing.properties incompleto (storeFile, keyAlias)" -ForegroundColor Yellow
    } else {
        $keystorePath = Join-Path $root $storeFile
        if (-not (Test-Path $keystorePath)) {
            Write-Host "Keystore no encontrado: $keystorePath" -ForegroundColor Yellow
        } else {
            Write-Host "Keystore: $keystorePath  Alias: $alias" -ForegroundColor DarkGray
            Write-Host "Introduce la contraseña del keystore cuando keytool la pida:" -ForegroundColor DarkGray
            keytool -list -v -keystore $keystorePath -alias $alias 2>&1 |
                Select-String "SHA1:" |
                ForEach-Object { $_.Line.Trim() }
        }
    }
}

Write-Host ""
Write-Host "Pega cada SHA-1 en Firebase -> Configuracion -> Huella digital." -ForegroundColor Green
Write-Host "Play Console -> Integridad de la app -> certificado de firma (SHA-1 de tienda)." -ForegroundColor Green
Write-Host ""
