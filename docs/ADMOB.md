# AdMob — configuración paso a paso

Guía para registrar la app en AdMob, crear unidades de anuncio y pegar los IDs en el proyecto. Incluye consentimiento UMP (GDPR/EEE) y cómo probar en debug vs release.

**Archivo que debes editar:** `app/src/main/res/values/admob.xml`  
**Debe coincidir con:** `applicationId` en `app.template.properties`

**Relacionado:** [FIREBASE.md](FIREBASE.md) (Analytics y vinculación opcional con Firebase)

---

## Decisión: ¿cuenta compartida o app independiente?

AdMob no funciona igual que Firebase. Para varias apps del mismo estudio:

| Elemento | Compartido | Independiente (por app) |
|----------|------------|-------------------------|
| **Cuenta de editor AdMob** | Una sola (`ca-app-pub-XXXXXXXX~...`) | No aplica — siempre la misma cuenta |
| **App en consola AdMob** | **Una por juego** | Cada juego = app nueva |
| **App ID** | Propio de cada app | Propio de cada app |
| **Unidades banner / intersticial** | Propias de cada app | Propias de cada app |
| **Repositorio Git** | Un repo por juego | Un repo por juego |
| **Firebase** | Puede ser proyecto compartido | Opcional |

### Recomendación para apps JMaker (Sudoku, Buscaminas, etc.)

- **Misma cuenta AdMob** que ya uses (p. ej. la de Buscaminas).
- **Nueva app** en AdMob por cada juego — **no reutilices** los IDs de banner/intersticial de otra app.
- Paquete Android = `applicationId` de `app.template.properties`.

---

## Paso 0 — Identidad en el repo (obligatorio)

Antes de AdMob, confirma `app.template.properties`:

| Clave | Ejemplo Sudoku |
|-------|----------------|
| `applicationId` | `com.jmaker.sudoku` |
| `appDisplayName` | `Sudoku` |

Ese paquete debe ser **idéntico** en AdMob, Firebase, Play Console y `google-services.json`.

---

## Paso 1 — Entrar en AdMob

1. Abre [AdMob](https://apps.admob.com/) con la cuenta del estudio.
2. Menú izquierdo → **Aplicaciones** (Apps).
3. Si ves otra app (p. ej. Buscaminas), **no entres en ella** para configurar Sudoku — vuelve a la lista y crea o abre la app correcta.

---

## Paso 2 — Registrar la app Android

1. **Aplicaciones** → **Añadir aplicación** (Add app).
2. Asistente **Configurar una nueva aplicación**:

| Campo | Valor |
|-------|--------|
| Plataforma | **Android** |
| ¿Publicada en tienda admitida? | **No** (hasta subir el AAB a Play) |
| Nombre | `Sudoku` (o tu `appDisplayName`) |
| Paquete Android | Tu `applicationId` (ej. `com.jmaker.sudoku`) |

3. Termina el asistente y crea la app.

### Sobre la revisión de AdMob

AdMob puede mostrar:

> *El servicio de anuncios se limita hasta que las aplicaciones se revisan y aprueban.*

Es normal en apps nuevas. La revisión suele tardar **unos días**. Mientras tanto:

- En **debug** sigues probando con **IDs de prueba** de Google (ver Paso 8).
- Tras publicar en Play (aunque sea **prueba interna**), la revisión suele completarse antes.

---

## Paso 3 — Copiar el App ID

1. Entra en la app **Sudoku** (no en otra).
2. Menú → **Configuración de la aplicación** (App settings).
3. Copia el **App ID** (formato `ca-app-pub-XXXXXXXX~YYYYYYYY`).

Ese valor va en `admob_app_id` en `admob.xml`.

También aparece en **AndroidManifest.xml** vía:

```xml
<meta-data android:name="com.google.android.gms.ads.APPLICATION_ID" ... />
```

No edites el manifest manualmente; usa `admob.xml`.

---

## Paso 4 — Crear unidad Banner

1. Dentro de la app Sudoku → **Bloques de anuncios** → **Añadir bloque de anuncios**.
2. Configuración recomendada:

| Campo | Valor |
|-------|--------|
| Formato | **Banner** |
| Nombre | `Sudoku Banner` (solo para ti en consola) |
| **Puja de partner** | **No marcar** (no uses mediación externa en este bloque) |
| Tipo de anuncio | Texto, imagen y rich media (+ vídeo si viene marcado) |
| Actualización automática | **Optimizado por Google** |
| eCPM mínimo | **Optimizado por Google** |
| Límite mínimo | **Todos los precios** (mejor relleno al lanzar) |

3. **Crear** y copia el ID del bloque (`ca-app-pub-XXXXXXXX/ZZZZZZZZ`).

Va en `admob_banner_unit_id`.

---

## Paso 5 — Crear unidad Intersticial

1. **Bloques de anuncios** → **Añadir bloque de anuncios** otra vez.
2. Configuración recomendada:

| Campo | Valor |
|-------|--------|
| Formato | **Intersticial** |
| Nombre | `Sudoku Interstitial` |
| **Puja de partner** | **No marcar** |
| Resto | Defaults / Optimizado por Google |

3. Copia el ID → `admob_interstitial_unit_id`.

---

## Paso 5b — Crear unidad Recompensado (pistas)

1. **Bloques de anuncios** → **Añadir bloque de anuncios**.
2. Configuración:

| Campo | Valor |
|-------|--------|
| Formato | **Recompensado** (Rewarded) |
| Nombre | `Sudoku Rewarded Hint` |
| **Puja de partner** | **No marcar** |

3. Copia el ID → `admob_rewarded_unit_id` en `admob.xml`.

La app otorga **1 pista** solo si el usuario **termina** el video (`RewardedAdManager` + pistas en `SudokuScreen.kt`).

---

## Paso 6 — Pegar IDs en el proyecto

Edita `app/src/main/res/values/admob.xml`:

```xml
<resources>
    <string name="admob_app_id" translatable="false">ca-app-pub-TU_EDITOR~TU_APP_ID</string>
    <string name="admob_banner_unit_id" translatable="false">ca-app-pub-TU_EDITOR/TU_BANNER</string>
    <string name="admob_interstitial_unit_id" translatable="false">ca-app-pub-TU_EDITOR/TU_INTERSTITIAL</string>
    <string name="admob_rewarded_unit_id" translatable="false">ca-app-pub-TU_EDITOR/TU_REWARDED</string>
</resources>
```

### Ejemplo Sudoku (referencia)

| Recurso | ID |
|---------|-----|
| `admob_app_id` | `ca-app-pub-8047251441069780~1265100923` |
| `admob_banner_unit_id` | `ca-app-pub-8047251441069780/3845208403` |
| `admob_interstitial_unit_id` | `ca-app-pub-8047251441069780/8105841448` |
| `admob_rewarded_unit_id` | Crear en AdMob → pegar en `admob.xml` |

No uses IDs de prueba de Google (`ca-app-pub-3940256099942544...`) en release.

---

## Paso 7 — Vincular con Firebase (opcional, recomendado)

Si ya configuraste Firebase ([FIREBASE.md](FIREBASE.md)):

1. AdMob → app Sudoku → **Configuración de la aplicación**.
2. **Vincular con Firebase** / Link to Firebase.
3. Elige el proyecto (ej. `buscaminas-minesweeper`) y la app `com.jmaker.sudoku`.

Mejora informes de ingresos y métricas en Analytics. **No es obligatorio** para que carguen anuncios.

---

## Paso 8 — Probar anuncios (debug vs release)

### Debug — IDs de prueba automáticos

En builds **DEBUG**, `AdIds.kt` **ignora** los IDs de producción y usa los de prueba de Google:

```kotlin
if (BuildConfig.DEBUG) TEST_BANNER else productionId
```

Instala debug:

```powershell
.\gradlew installDebug
```

Verás anuncios de prueba aunque `admob.xml` tenga IDs reales. Es el comportamiento esperado.

### Release — IDs reales

En **release** (AAB firmado, prueba interna en Play):

- Se usan los IDs de `admob.xml`.
- UMP y consentimiento se comportan como en producción.
- AdMob debe haber **aprobado** la app para servir anuncios reales.

Generar AAB (con `signing.properties` configurado):

```powershell
.\scripts\build-release-aab.ps1
```

---

## Paso 9 — Consentimiento UMP (GDPR / EEE)

> **PENDIENTE (Buscaminas + Sudoku):** configurar en AdMob → **Privacidad y mensajes** → **Reglamentos europeos**. Un mensaje por app (o mensajes separados con la URL de privacidad correcta en cada uno). Opciones recomendadas: Consentimiento ON, Gestionar opciones ON, No consentir ON, Cerrar (no consentir) ON. Publicar y vincular la app en **Tus aplicaciones**.

| App | Paquete | URL privacidad |
|-----|---------|----------------|
| **Buscaminas** | `com.jmaker.buscaminasarcade` | https://j-maker.github.io/busccaminas-arcade/ |
| **Sudoku** | `com.jmaker.sudoku` | https://j-maker.github.io/sudoku/ |

La app ya implementa UMP en `ads/AdConsentManager.kt` antes de inicializar AdMob. Falta terminar la configuración en consola:

1. AdMob → **Privacidad y mensajes** (Privacy & messaging) — nivel de cuenta.
2. **Crear mensaje** → GDPR / regulaciones europeas (+ Reino Unido y Suiza si aplica).
3. Enlace a política de privacidad: URL pública de `legal.xml` → `privacy_policy_url`.
4. **Publicar** el mensaje.
5. **Asociarlo** a la app Sudoku / paquete `com.jmaker.sudoku`.

Sin este paso, en la UE el formulario de consentimiento puede no mostrarse correctamente en producción.

### Flujo en la app

1. Al abrir → `AdConsentManager.gatherConsentAndInitializeAds`
2. Tras consentimiento (o si no aplica) → `MobileAds.initialize`
3. Banner e intersticial cargan después

---

## Paso 10 — Verificar en Analytics (opcional)

Con la app en uso, Firebase DebugView puede mostrar eventos como:

- `sudoku_ad_banner_load`
- `sudoku_ad_banner_impr`
- `sudoku_ad_inter_load`
- `sudoku_ad_inter_show`

Confirma que AdMob y Analytics están conectados (especialmente si vinculaste Firebase en el Paso 7).

---

## Navegación rápida en AdMob

```
Inicio
  └── Aplicaciones
        ├── Buscaminas          ← otra app (no mezclar IDs)
        └── Sudoku              ← tu app
              ├── Bloques de anuncios           → banner + intersticial
              ├── Configuración de la aplicación → App ID, Firebase
              └── Informes                      → ingresos (post-lanzamiento)

Privacidad y mensajes  ← UMP (cuenta, luego asociar a Sudoku)
```

---

## Errores frecuentes

| Síntoma | Causa probable | Solución |
|---------|----------------|----------|
| No hay anuncios en release | App AdMob aún en revisión | Esperar aprobación; subir AAB a Play |
| Anuncios de prueba en debug aunque cambiaste `admob.xml` | Comportamiento de `AdIds.kt` | Normal en DEBUG |
| IDs de Buscaminas en Sudoku | Reutilizar unidades de otra app | Crear app y unidades nuevas en AdMob |
| Paquete distinto en AdMob | Error al registrar | Debe ser exactamente `applicationId` |
| UMP no aparece en UE | Mensaje no publicado o no vinculado | Paso 9 en consola |
| Crash al iniciar | App ID incorrecto en manifest | Revisa `admob_app_id` en `admob.xml` |
| Marcaste «Puja de partner» | Bloque limitado a mediación externa | Crear un bloque nuevo sin esa casilla |

---

## Checklist AdMob

- [ ] `applicationId` definido en `app.template.properties`
- [ ] App **Sudoku** creada en AdMob (no confundir con otra app)
- [ ] Paquete = `applicationId` (ej. `com.jmaker.sudoku`)
- [ ] Elegido **No** en «¿publicada en tienda?» si aún no está en Play
- [ ] App ID → `admob_app_id`
- [ ] Unidad Banner → `admob_banner_unit_id`
- [ ] Unidad Intersticial → `admob_interstitial_unit_id`
- [ ] Unidad Recompensado → `admob_rewarded_unit_id` (pistas)
- [ ] `admob.xml` actualizado (sin IDs de prueba `3940256099942544` en release)
- [ ] (Opcional) Firebase vinculado en AdMob
- [ ] Mensaje UMP publicado y asociado a la app
- [ ] `privacy_policy_url` en `legal.xml` apunta a URL pública real
- [ ] Debug probado (`installDebug` + anuncios de prueba)
- [ ] Release probado vía prueba interna cuando tengas AAB

---

## Referencias en este repo

| Archivo | Rol |
|---------|-----|
| `app/src/main/res/values/admob.xml` | App ID, banner, intersticial, recompensado |
| `app/src/main/java/.../ads/AdIds.kt` | IDs de prueba en DEBUG |
| `app/src/main/java/.../ads/AdConsentManager.kt` | UMP / consentimiento |
| `app/src/main/java/.../ads/BannerAdView.kt` | Banner |
| `app/src/main/java/.../ads/InterstitialAdManager.kt` | Intersticial |
| `app/src/main/java/.../ads/RewardedAdManager.kt` | Recompensado (pistas) |
| `app/src/main/res/values/legal.xml` | URL política de privacidad (UMP y Play) |
| `app.template.properties` | `applicationId` |

**Siguiente paso del checklist:** `legal.xml` (política de privacidad) y firma release para el AAB.
