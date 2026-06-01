# Base Android — Ads · Firebase · Play

Plantilla para publicar apps en Google Play con **AdMob**, **Firebase Analytics**, **UMP**, **Play Integrity**, **Play Games** (opcional), **14 idiomas**, **tutorial inicial** y **recordatorio diario**.

> **Estado:** repo base listo para bifurcar y crear apps nuevas. Los placeholders (Firebase, AdMob de prueba) son intencionados; sustitúyelos por app antes de subir a Play Store.

## Inicio rápido

```bat
gradlew.bat installDebug
```

1. Edita `app.template.properties` (paquete, versión, Analytics).
2. Sustituye `app/google-services.json` y `app/src/main/res/values/admob.xml`.
3. Reemplaza `ui/TemplateHomeScreen.kt` por tu app.

En **DEBUG** los anuncios usan IDs de prueba (`AdIds.kt`). En release se usan los de `admob.xml`.

En PowerShell, desde la raíz del proyecto:

```powershell
.\gradlew.bat installDebug
```

---

## Checklist antes de publicar (cada app nueva)

| Paso | Archivo / acción |
|------|------------------|
| Paquete, nombre, versión, slug GA4 | `app.template.properties` |
| Firebase real (mismo `applicationId`) | `app/google-services.json` |
| IDs AdMob de producción | `app/src/main/res/values/admob.xml` |
| Política de privacidad (URL pública) | `app/src/main/res/values/legal.xml` |
| Pantalla principal de tu producto | `app/.../ui/TemplateHomeScreen.kt` |
| Firma release | `signing.properties` (no subir a git) |
| AAB firmado | `.\scripts\build-release-aab.ps1` |
| Icono 512 px y capturas | `store-assets/icons/`, `store-assets/screenshots/` |

---

## Qué archivo reemplazar

### 1. Identidad y versión

**`app.template.properties`** (raíz)

| Clave | Qué poner | Ejemplo |
|-------|-----------|---------|
| `applicationId` | Paquete en Play Store | `com.miempresa.miapp` |
| `appSlug` | ID en GA4 (sin espacios) | `mi_app` |
| `appDisplayName` | Nombre en Analytics y launcher | `Mi App` |
| `versionCode` | Entero por subida | `1` |
| `versionName` | Versión visible | `1.0.0` |

Gradle lee este archivo solo; no hace falta editar `build.gradle.kts` para el paquete.

### 2. Firebase

**`app/google-services.json`**

1. [Firebase Console](https://console.firebase.google.com/) → Añadir app Android.
2. Paquete = `applicationId` del paso 1.
3. Descargar JSON y reemplazar el archivo en `app/`.

### 3. AdMob

**`app/src/main/res/values/admob.xml`**

| Recurso | Origen |
|---------|--------|
| `admob_app_id` | AdMob → Apps |
| `admob_banner_unit_id` | Unidad Banner |
| `admob_interstitial_unit_id` | Unidad Intersticial |

Registra la app en [AdMob](https://apps.admob.com/) con el mismo paquete. UMP: AdMob → Privacidad (código en `ads/AdConsentManager.kt`).

### 4. Privacidad y Play Games

**`app/src/main/res/values/legal.xml`**

| Recurso | Uso |
|---------|-----|
| `privacy_policy_url` | URL pública de la política |
| `privacy_contact_email` | Email de contacto |
| `play_games_project_id` | Vacío = desactivado |
| `play_games_leaderboard_default` | Vacío = desactivado |

### 5. Tu app

| Qué | Archivo |
|-----|---------|
| Pantalla principal | `app/.../ui/TemplateHomeScreen.kt` |
| Flujo | `app/.../ui/MainScreen.kt` |
| Splash al abrir | `values-v31/themes.xml` + `ic_splash.xml` (sistema Android 12+) |
| Banner | `BannerAdView` en `TemplateHomeScreen` |
| Intersticial | `ads/InterstitialAdManager.kt` |

### 6. Firma release

- `signing.properties.example` → `signing.properties` (no subir a git)
- `.\scripts\build-release-aab.ps1`

### 7. Play Store (fuera del APK)

| Material | Carpeta |
|----------|---------|
| Icono 512 px | `store-assets/icons/` |
| Capturas | `store-assets/screenshots/` |
| Política web | Publicar HTML; URL en `legal.xml` |

---

## 14 idiomas (multipaís)

| Carpeta | Código | Idioma |
|---------|--------|--------|
| `values/` | `es` | Español (por defecto) |
| `values-en/` | `en` | Inglés |
| `values-pt/` | `pt` | Portugués |
| `values-fr/` | `fr` | Francés |
| `values-de/` | `de` | Alemán |
| `values-it/` | `it` | Italiano |
| `values-ru/` | `ru` | Ruso |
| `values-tr/` | `tr` | Turco |
| `values-ar/` | `ar` | Árabe (RTL) |
| `values-hi/` | `hi` | Hindi |
| `values-in/` | `id` | Indonesio |
| `values-ja/` | `ja` | Japonés |
| `values-ko/` | `ko` | Coreano |
| `values-zh-rCN/` | `zh` | Chino simplificado |

**Archivos de textos** (traducir en las 14 carpetas):

| Archivo | Contenido |
|---------|-----------|
| `values-*/strings.xml` | Home, notificaciones, botón Ajustes |
| `values-*/strings_features.xml` | Tutorial, recordatorio, ajustes |

**Automático:** idioma del teléfono al instalar (`LocaleHelper.kt`, `AppDataStore.kt`); árabe RTL; AAB incluye todos los idiomas (`bundle.language.enableSplit = false`).

**Nombre de la app:** `appDisplayName` en `app.template.properties`, o `app_name` en cada `values-XX/strings.xml`.

**Traducciones:** guarda siempre los XML en **UTF-8** (Android Studio lo hace por defecto). No uses scripts de PowerShell que reescriban `strings*.xml` sin UTF-8, o se corrompen tildes y caracteres no latinos.

---

## Tutorial y recordatorio

**Flujo:** Splash del sistema (icono) → Tutorial (4 pasos, solo la 1.ª vez) → Diálogo recordatorio (opcional) → Pantalla principal. Botón **Ajustes** (arriba a la derecha).

**Splash al abrir:** en Android 12+ usa la splash nativa (`Theme.App.Starting`, `ic_splash.xml`, `installSplashScreen()` en `MainActivity`). No hay segunda splash en Compose; solo texto en tutorial/home.

| Función | Textos (`strings_features.xml`) | Código |
|---------|--------------------------------|--------|
| Tutorial | `tutorial_step1` … `tutorial_step4` | `ui/onboarding/OnboardingTutorialOverlay.kt` |
| ¿Recordatorio diario? | `reminder_prompt_*` | `ui/onboarding/ReminderOptInDialog.kt` |
| Ajustes (idioma + hora) | `settings_*` | `ui/settings/SettingsSheet.kt` |
| Notificación enviada | `notification_*` en `strings.xml` | `engagement/DailyReminderScheduler.kt` |

---

## Resumen de rutas

```
app.template.properties          ← paquete, slug GA4, versión, nombre
app/google-services.json         ← Firebase
app/src/main/res/values/admob.xml
app/src/main/res/values/legal.xml
app/src/main/res/values/strings.xml + values-XX/ (+ strings_features.xml)
app/.../ui/TemplateHomeScreen.kt ← tu app
signing.properties               ← firma (local, no git)
```

**No editar** en uso normal: `build.gradle.kts`, `AdIds.kt`, `AndroidManifest.xml`.

---

## Estructura del código

```
app/src/main/java/com/template/android/
├── ads/              AdMob, UMP, banner, intersticial
├── analytics/        Firebase (lee APP_SLUG de Gradle)
├── data/             Preferencias, idioma, tutorial visto
├── engagement/       Recordatorio, Play Games, review
├── security/         Play Integrity
├── ui/               Main, plantilla, onboarding, ajustes
└── TemplateApp.kt
```

---

## Icono de la app

| Recurso | Uso |
|---------|-----|
| `drawable/ic_launcher_foreground.xml` + `ic_launcher_background.xml` | Icono en el launcher (adaptativo) |
| `drawable/ic_splash.xml` | Splash al abrir (Android 12+) |
| `drawable/ic_notification.xml` | Icono blanco en notificaciones |

Personaliza el dibujo en esos XML (o sustituye por PNG en `mipmap-*` si lo prefieres). Si el launcher sigue mostrando un icono viejo tras instalar, desinstala la app y vuelve a instalar (caché del sistema).

---

## Funciones opcionales (incluidas, no obligatorias)

| Función | Estado en la plantilla | Cómo activarla |
|---------|------------------------|----------------|
| **Play Games** | Código listo; desactivado si los IDs están vacíos | Rellena `play_games_project_id` y `play_games_leaderboard_default` en `legal.xml` |
| **Play Integrity** | Se ejecuta al arrancar; omitido en debug | Sin config extra; en release verifica en servidor si lo necesitas |
| **Valoración en Play Store** | `PlayReviewHelper.kt` existe | Llámalo desde tu app cuando quieras (p. ej. tras varias sesiones); no hay botón en la plantilla |
| **Intersticial de prueba** | Botón en `TemplateHomeScreen` | Quita el botón al publicar; usa `InterstitialAdManager.showIfReady()` desde tu flujo |

---

## Qué incluye la base (ya implementado)

- AdMob: banner, intersticial, UMP (consentimiento GDPR/EEE)
- Firebase Analytics con `app_slug` por app (`app.template.properties`)
- Tutorial de 4 pasos (solo la primera vez)
- Recordatorio diario opcional + permiso de notificaciones
- Ajustes: idioma (14) y hora del recordatorio
- Tema Material oscuro neutro (sin branding de juego concreto)
- `applicationId` configurable sin tocar `build.gradle.kts`

---

## Importante

- No subas `signing.properties` ni keystores a git.
- `google-services.json` de la plantilla es solo ejemplo; usa el real antes de publicar.
- Tras cambiar `applicationId`, actualiza AdMob, Firebase y Play Console con el mismo paquete.
- Renombrar paquete Java (`com.template.android`) es opcional; Refactor en Android Studio. Para la mayoría de apps basta `applicationId` en `app.template.properties`.
- Si tienes otra app instalada con otro paquete (p. ej. un juego anterior), no la confundas al probar: revisa el nombre bajo el icono y el `applicationId` en `app.template.properties`.
