# Publicación 1.0.0 — Sudoku Neon

**Paquete:** `com.jmaker.sudoku`  
**versionCode:** 1 · **versionName:** 1.0.0  
**Versión en:** `app.template.properties`

## Antes de compilar

1. **Keystore:** `keystore/release.keystore` + `signing.properties` (ver `signing.properties.example`).
2. **Firebase:** SHA-1 de la firma release en la app `com.jmaker.sudoku`; descargar `google-services.json` actualizado.
3. **AdMob UMP:** mensaje GDPR publicado y vinculado a Sudoku ([CONSENTIMIENTO-ADMOB.md](../docs/CONSENTIMIENTO-ADMOB.md)).

## Generar AAB versionado

```powershell
.\scripts\stage-release-aab.ps1
```

Salida esperada:

- `store-assets/release/1.0.0/sudoku-1.0.0-v1.aab`
- `store-assets/release/1.0.0/native-debug-symbols.zip` (si Gradle lo genera)

## Play Console

| Campo | Origen |
|-------|--------|
| Notas de versión | `store-assets/release-notes-1.0.0.txt` |
| Título / descripciones | `store-assets/listing-es.txt` |
| Política de privacidad | https://j-maker.github.io/sudoku/ |
| Icono 512 px | `store-assets/icons/` |
| Capturas | `store-assets/screenshots/` (**pendiente**) |

## Checklist rápido

- [ ] AAB firmado subido (prueba interna)
- [ ] Seguridad de los datos completada
- [ ] Ficha de tienda (textos + capturas)
- [ ] Clasificación de contenido
- [ ] Países / producción

Guía completa: [docs/CHECKLIST-PUBLICACION.md](../docs/CHECKLIST-PUBLICACION.md)
