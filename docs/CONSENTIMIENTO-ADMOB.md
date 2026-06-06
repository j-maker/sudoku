# Consentimiento de anuncios (UMP / GDPR) — Sudoku

La app ya implementa UMP en `AdConsentManager.kt`. **Falta solo la consola AdMob.**

| Dato | Valor |
|------|--------|
| Paquete | `com.jmaker.sudoku` |
| URL privacidad | https://j-maker.github.io/sudoku/ |
| App ID | `ca-app-pub-8047251441069780~1265100923` |

## AdMob Console (obligatorio)

1. [AdMob](https://apps.admob.com/) → menú **Privacidad y mensajes** (Privacy & messaging).
2. **Reglamentos europeos** (European regulations) → **Crear mensaje** (o editar uno solo para Sudoku).
3. Rellena:
   - **URL de la política de privacidad:** `https://j-maker.github.io/sudoku/`
   - **Consentimiento:** activado
   - **Gestionar opciones:** activado
   - **No consentir:** activado
   - **Cerrar (no consentir):** activado
4. **Tus aplicaciones** → vincula la app **Sudoku** / paquete `com.jmaker.sudoku`.
5. **Publicar** el mensaje.

> Recomendado: un mensaje **por app** (Sudoku y Buscaminas por separado, cada uno con su URL).

## En la app (ya hecho)

| Pieza | Ubicación |
|-------|-----------|
| Consentimiento al abrir | `MainActivity` → `AdConsentManager.gatherConsentAndInitializeAds` |
| Cambiar preferencias | Ajustes → **Gestionar consentimiento de anuncios** |
| IDs AdMob | `app/src/main/res/values/admob.xml` |
| URL privacidad in-app | `legal.xml` → `privacy_policy_url` |

## Probar en debug

En builds **DEBUG**, `AdConsentManager` simula geografía **EEE** → deberías ver el formulario UMP al abrir la app (sin VPN).

```powershell
.\gradlew.bat installDebug
```

Logcat filtro: `AdConsent`

## Tras publicar en consola

Marca en [CHECKLIST-PUBLICACION.md](CHECKLIST-PUBLICACION.md) §3:

- [ ] Mensaje europeo publicado
- [ ] App `com.jmaker.sudoku` vinculada
- [ ] Opciones No consentir + Cerrar activadas

Más detalle: [ADMOB.md](ADMOB.md) — Paso 9.
