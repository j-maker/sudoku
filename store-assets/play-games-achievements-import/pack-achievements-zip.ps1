# Empaqueta logros Play Games para importar en Play Console.
$ErrorActionPreference = "Stop"
$here = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $here

$icons = @(
    "first_win.png",
    "ten_wins.png",
    "hard_mode.png",
    "win_streak.png",
    "seven_day_player.png",
    "daily_challenge.png"
)

foreach ($icon in $icons) {
    if (-not (Test-Path $icon)) {
        Write-Host "Falta: $icon" -ForegroundColor Red
        Write-Host "Lee ICONOS-LEEME.txt"
        exit 1
    }
}

$zipPath = Join-Path (Split-Path $here -Parent) "sudoku-achievements-import.zip"
if (Test-Path $zipPath) { Remove-Item $zipPath -Force }

$files = @(
    "AchievementsMetadata.csv",
    "AchievementsLocalizations.csv",
    "AchievementsIconsMappings.csv"
) + $icons

Compress-Archive -Path $files -DestinationPath $zipPath -Force
Write-Host "Listo: $zipPath" -ForegroundColor Green

# ZIP sin traducciones (si falla el CSV de idiomas)
$zipNoI18n = Join-Path (Split-Path $here -Parent) "sudoku-achievements-import-no-i18n.zip"
if (Test-Path $zipNoI18n) { Remove-Item $zipNoI18n -Force }
$filesNoI18n = @(
    "AchievementsMetadata.csv",
    "AchievementsIconsMappings.csv"
) + $icons
Compress-Archive -Path $filesNoI18n -DestinationPath $zipNoI18n -Force
Write-Host "Alternativo (sin idiomas): $zipNoI18n" -ForegroundColor Yellow
