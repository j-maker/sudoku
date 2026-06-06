<#
.SYNOPSIS
  Captura capturas de Play Store desde un dispositivo/emulador Android vía adb.

.DESCRIPTION
  Guarda PNG en store-assets/screenshots/ con nombres phone-01-*.png … phone-08-*.png.
  Requisitos: adb en PATH, un dispositivo conectado (adb devices), app instalada.

.EXAMPLE
  .\scripts\capture-screenshots.ps1
  Modo guiado: 8 pantallas con pausa entre cada una.

.EXAMPLE
  .\scripts\capture-screenshots.ps1 -InstallDebug
  Instala debug en el dispositivo y luego abre el modo guiado.

.EXAMPLE
  .\scripts\capture-screenshots.ps1 -Index 3
  Recaptura solo la captura #3 (reto diario).

.EXAMPLE
  .\scripts\capture-screenshots.ps1 -One -Name "phone-04-victoria.png"
  Una sola captura con nombre personalizado.
#>
param(
    [switch]$InstallDebug,
    [int]$Index = 0,
    [string]$One,
    [switch]$ListOnly
)

$ErrorActionPreference = "Stop"
$Root = Split-Path $PSScriptRoot -Parent
$OutDir = Join-Path $Root "store-assets\screenshots"
$RemotePath = "/sdcard/sudoku_play_screenshot.png"

$Shots = @(
    @{
        File = "phone-01-juego.png"
        Hint = "Partida en curso: tablero con numeros, tiempo y dificultad visibles."
    },
    @{
        File = "phone-02-dificultad.png"
        Hint = "Selector de dificultad (Facil / Medio / Dificil) antes de empezar o al cambiar modo."
    },
    @{
        File = "phone-03-reto-diario.png"
        Hint = "Tarjeta de reto diario y racha de dias (badge bajo el titulo)."
    },
    @{
        File = "phone-04-victoria.png"
        Hint = "Dialogo de victoria al completar un sudoku."
    },
    @{
        File = "phone-05-estadisticas.png"
        Hint = "Panel de estadisticas / logros (icono de trofeo o stats)."
    },
    @{
        File = "phone-06-ajustes.png"
        Hint = "Hoja de ajustes: idioma, sonido, recordatorio, tema."
    },
    @{
        File = "phone-07-tutorial.png"
        Hint = "Tutorial inicial (overlay de bienvenida). Reinstala debug o borra datos si ya lo viste."
    },
    @{
        File = "phone-08-pistas.png"
        Hint = "Pistas / hints visibles o boton de ver anuncio por pista extra."
    }
)

function Write-Step([string]$Message, [string]$Color = "Cyan") {
    Write-Host $Message -ForegroundColor $Color
}

function Test-AdbReady {
    if (-not (Get-Command adb -ErrorAction SilentlyContinue)) {
        Write-Host "adb no esta en PATH. Instala Android SDK platform-tools." -ForegroundColor Red
        exit 1
    }

    $devices = adb devices | Select-Object -Skip 1 | Where-Object { $_ -match "\tdevice$" }
    if (-not $devices) {
        Write-Host "No hay dispositivo Android conectado. Ejecuta: adb devices" -ForegroundColor Red
        Write-Host "Conecta un telefono con depuracion USB o abre un emulador (1080x2400 recomendado)." -ForegroundColor Yellow
        exit 1
    }

    $serial = ($devices[0] -split "\t")[0]
    Write-Step "Dispositivo: $serial" "DarkGray"
}

function Invoke-AndroidScreenshot([string]$DestPath) {
    adb shell screencap -p $RemotePath | Out-Null
    if ($LASTEXITCODE -ne 0) { throw "adb screencap fallo" }

    adb pull $RemotePath $DestPath | Out-Null
    if ($LASTEXITCODE -ne 0) { throw "adb pull fallo" }

    adb shell rm $RemotePath | Out-Null

    if (-not (Test-Path $DestPath)) {
        throw "No se creo el archivo: $DestPath"
    }

    $sizeKb = [math]::Round((Get-Item $DestPath).Length / 1KB, 1)
    Write-Host "  -> $DestPath ($sizeKb KB)" -ForegroundColor Green
}

function Show-ShotList {
    Write-Host ""
    Write-Step "Capturas planificadas ($($Shots.Count)):" 
    for ($i = 0; $i -lt $Shots.Count; $i++) {
        $n = $i + 1
        Write-Host ("  {0,2}. {1}" -f $n, $Shots[$i].File) -ForegroundColor White
        Write-Host ("      {0}" -f $Shots[$i].Hint) -ForegroundColor DarkGray
    }
    Write-Host ""
}

Push-Location $Root
try {
    New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

    if ($ListOnly) {
        Show-ShotList
        exit 0
    }

    if ($InstallDebug) {
        Write-Step "Instalando app debug ..."
        & .\gradlew.bat installDebug
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
        Write-Step "Abre Sudoku en el dispositivo antes de capturar." "Yellow"
        Write-Host ""
    }

    Test-AdbReady

    Write-Host ""
    Write-Step "Destino: $OutDir"
    Write-Host "Consejo: emulador 1080x2400, modo vertical, sin datos personales en la barra de estado." -ForegroundColor DarkGray
    Write-Host ""

    if ($One) {
        $fileName = if ($One -match '\.png$') { $One } else { "$One.png" }
        Read-Host "Prepara la pantalla y pulsa Enter para capturar $fileName"
        Invoke-AndroidScreenshot (Join-Path $OutDir $fileName)
        exit 0
    }

    $targets = if ($Index -gt 0) {
        if ($Index -lt 1 -or $Index -gt $Shots.Count) {
            Write-Host "Index fuera de rango (1-$($Shots.Count))." -ForegroundColor Red
            exit 1
        }
        ,$Shots[$Index - 1]
    } else {
        $Shots
    }

    if ($Index -eq 0) { Show-ShotList }

    $num = if ($Index -gt 0) { $Index } else { 1 }
    foreach ($shot in $targets) {
        Write-Step ("[{0}/{1}] {2}" -f $num, $Shots.Count, $shot.File)
        Write-Host $shot.Hint -ForegroundColor Yellow
        $answer = Read-Host "Prepara esa pantalla en el movil y pulsa Enter (S = saltar)"
        if ($answer -match '^[sS]$') {
            Write-Host "  Omitida." -ForegroundColor DarkGray
            $num++
            continue
        }
        Invoke-AndroidScreenshot (Join-Path $OutDir $shot.File)
        $num++
        Write-Host ""
    }

    Write-Host ""
    Write-Step "Listo. Revisa las imagenes en store-assets\screenshots\" "Green"
    Write-Host "Play Store: PNG/JPEG, lado largo 1080-7680 px, vertical 9:16." -ForegroundColor DarkGray
}
finally {
    Pop-Location
}
