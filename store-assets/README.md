# Materiales de Google Play — Sudoku

| Archivo / carpeta | Uso |
|-------------------|-----|
| `privacy.html` | Subir a GitHub Pages → URL de política |
| `listing-es.txt` | Copiar textos a Play Console |
| `icons/app-icon-source.png` | Fuente del icono (1024×1024) |
| `icons/play-store-icon-512.png` | Icono de tienda 512×512 |
| `screenshots/phone-*.png` | Capturas de teléfono (**pendiente**) |
| `release/1.0.0/sudoku-1.0.0-v1.aab` | **AAB listo para subir** (versionCode 1) |
| `release/1.0.0/native-debug-symbols.zip` | Símbolos nativos para Play Console |
| `RELEASE-1.0.0.md` | Guía paso a paso para esta publicación |
| `release-notes-1.0.0.txt` | Notas de versión **1.0.0** (todos los idiomas Play) |

Generar y copiar AAB a `release/<version>/`:

```powershell
.\scripts\stage-release-aab.ps1
```

Guía completa: [docs/CHECKLIST-PUBLICACION.md](../docs/CHECKLIST-PUBLICACION.md)
