# Firebase — configuración paso a paso

Guía para conectar esta app a Firebase Analytics. Incluye cuándo usar un **proyecto compartido** (varias apps en el mismo Firebase) o uno **independiente** (un Firebase por app).

**Archivo que debes sustituir:** `app/google-services.json`  
**Debug y release** comparten el mismo paquete y el mismo JSON (listo para producción).  
**Debe coincidir con:** `applicationId` en `app.template.properties`

---

## Decisión: ¿proyecto compartido o independiente?

Antes de entrar en Firebase Console, elige una estrategia. **No depende del repositorio Git**: cada repo lleva su propio `google-services.json`; lo que cambia es si todas las apps apuntan al mismo proyecto Firebase o no.

| Criterio | Proyecto **compartido** (agrupado) | Proyecto **independiente** |
|----------|-----------------------------------|----------------------------|
| Varios juegos del mismo estudio / cuenta Google | Recomendado | Posible, pero más gestión |
| Repos Git separados | Compatible | Compatible |
| Analytics (GA4) | Un solo panel; filtras por `app_slug` | Un panel por app |
| Consola Firebase | Una sola | Una por app |
| `google-services.json` | Puede listar varios `package_name` | Solo un `package_name` |
| Marcas o empresas distintas | No ideal | Recomendado |
| Venta o transferencia de una app | Más enredado | Más limpio |
| Encaja con esta plantilla | Sí — diseñada para multi-app | Sí, pero pierdes el filtro unificado |

### Recomendación para apps JMaker (Sudoku, Buscaminas, etc.)

Usa **proyecto compartido**: registra cada app Android en el mismo Firebase (p. ej. `buscaminas-minesweeper`) y diferencia en Analytics con `appSlug` en `app.template.properties`.

Esta plantilla envía en cada evento el parámetro `app_slug` (ver `analytics/AppAnalytics.kt`). En GA4 puedes filtrar por `app_slug = sudoku`, `app_slug = buscaminas_arcade`, etc.

---

## Paso 0 — Configurar identidad en el repo (obligatorio)

Edita `app.template.properties` **antes** de Firebase:

| Clave | Qué es | Ejemplo Sudoku |
|-------|--------|----------------|
| `applicationId` | Paquete en Play, AdMob y Firebase | `com.jmaker.sudoku` |
| `appSlug` | ID único en GA4 (sin espacios) | `sudoku` |
| `appDisplayName` | Nombre visible en Analytics | `Sudoku` |

Reglas:

- El `applicationId` debe ser **único** en Play Store.
- El `appSlug` debe ser **único dentro del mismo proyecto Firebase** (no repitas el de otra app tuya).
- No edites `build.gradle.kts` para cambiar el paquete; Gradle lee `app.template.properties`.
- **No pongas comentarios en la misma línea** que un valor (`appSlug=sudoku # comentario`). En archivos `.properties`, el `#` inline forma parte del valor. Usa líneas `#` aparte.

---

## Opción A — Proyecto compartido (recomendado)

Usa esta opción si ya tienes un Firebase con otra app del estudio (p. ej. Buscaminas en `buscaminas-minesweeper`).

### A.1 — Abrir el proyecto existente

1. Entra en [Firebase Console](https://console.firebase.google.com/).
2. Abre el proyecto que ya uses (ej. **`buscaminas-minesweeper`**).
3. En ⚙️ **Configuración del proyecto** → **General**, comprueba las apps Android ya registradas.

### A.2 — Registrar esta app como nueva app Android

1. En la página principal del proyecto → **Agregar app** → icono **Android**.
2. Rellena:

| Campo | Valor |
|-------|--------|
| Nombre del paquete de Android | Tu `applicationId` (ej. `com.jmaker.sudoku`) |
| Alias de la app | Tu `appDisplayName` (ej. `Sudoku`) |
| Certificado SHA-1 | Vacío por ahora (ver sección SHA-1 al final) |

3. Pulsa **Registrar app**.

### A.3 — Descargar `google-services.json`

1. Descarga el archivo cuando Firebase lo ofrezca.
2. También puedes obtenerlo después en ⚙️ **Configuración del proyecto** → app Android → **google-services.json**.

Es **normal** que el JSON contenga **varios bloques** en `"client"` (uno por cada app Android del proyecto):

```json
"client": [
  { "package_name": "com.jmaker.buscaminasarcade" },
  { "package_name": "com.jmaker.sudoku" }
]
```

Gradle usa solo el bloque cuyo `package_name` coincide con el `applicationId` de la app que compilas.

### A.4 — Colocar el archivo en el repo

1. Copia el JSON descargado a:

```
app/google-services.json
```

2. Sustituye por completo el placeholder de la plantilla.

### A.5 — Saltar «Agregar el SDK de Firebase» en la consola

Firebase muestra pasos para añadir dependencias Gradle. **En esta plantilla ya están integradas:**

- Plugin `com.google.gms.google-services` en `app/build.gradle.kts`
- `firebase-bom` y `firebase-analytics` en dependencias

En la consola: **Siguiente** → **Siguiente** → **Ir a la consola**. No copies código de Firebase al proyecto.

### A.6 — Verificar

En el JSON debe aparecer:

```json
"project_id": "buscaminas-minesweeper"
```

y un cliente con:

```json
"package_name": "com.jmaker.sudoku"
```

Compila desde la raíz del proyecto:

```powershell
.\gradlew assembleDebug
```

Si el paquete no coincide, el plugin de Google Services fallará en el build.

### A.7 — No tocar el otro repo (importante)

Cada repositorio tiene su propio `app/google-services.json`. **No es obligatorio** actualizar el JSON de Buscaminas al añadir Sudoku: cada app compila con su `applicationId` y el plugin elige el cliente correcto del JSON.

Opcional: si quieres el JSON más reciente en ambos repos, descarga de nuevo desde Firebase y reemplázalo en cada uno (el contenido será equivalente).

---

## Opción B — Proyecto independiente

Usa esta opción si quieres Firebase totalmente separado para esta app (otra marca, otro equipo, venta futura, etc.).

### B.1 — Crear proyecto nuevo

1. [Firebase Console](https://console.firebase.google.com/) → **Crear un proyecto**.
2. Nombre sugerido: nombre de la app o del estudio (ej. `Sudoku` o `JMaker Sudoku`).
3. Google Analytics: **activar** (recomendado).
4. Elige o crea una cuenta de Analytics.
5. **Crear proyecto**.

### B.2 — Registrar la app Android

1. **Agregar app** → **Android**.
2. Paquete = `applicationId` de `app.template.properties`.
3. **Registrar app** → descargar `google-services.json`.

### B.3 — Colocar el archivo y verificar

1. Reemplaza `app/google-services.json`.
2. El JSON tendrá un solo `"client"` (solo esta app).
3. `project_id` será el del proyecto nuevo (distinto al de otras apps).
4. Compila: `.\gradlew assembleDebug`.
5. En la consola, salta los pasos del SDK (igual que en la opción A).

`appSlug` en `app.template.properties` sigue siendo útil para nombrar eventos; en un proyecto independiente no necesitas filtrar entre apps, pero no perjudica dejarlo configurado.

---

## Comprobar Analytics (opcional)

### DebugView

1. Instala debug: `.\gradlew installDebug`
2. Activa modo debug en el dispositivo:

```powershell
adb shell setprop debug.firebase.analytics.app com.jmaker.sudoku
```

(Sustituye el paquete por tu `applicationId`.)

3. Firebase Console → **Analytics** → **DebugView**.
4. Abre la app; deberías ver eventos con parámetro **`app_slug`** = valor de `app.template.properties`.

Para desactivar:

```powershell
adb shell setprop debug.firebase.analytics.app .none.
```

### Filtros en GA4 (proyecto compartido)

En informes o exploraciones, filtra por:

- Parámetro de evento: `app_slug`
- Valor: tu `appSlug` (ej. `sudoku`)

Así separas Sudoku de Buscaminas u otras apps en el mismo Firebase.

---

## SHA-1 (recomendado al publicar)

Debug y release usan **`com.jmaker.sudoku`** y el mismo `app/google-services.json`.

En Firebase → ⚙️ Configuración → app **`com.jmaker.sudoku`** → **Agregar huella digital**:

| Huella | Origen | Estado Sudoku |
|--------|--------|---------------|
| SHA-1 debug | `.\scripts\print-firebase-sha1.ps1` (sección DEBUG) | Hecho en consola |
| SHA-1 release | Mismo script (sección RELEASE, tras `signing.properties`) | Pendiente al firmar AAB |

Huella debug de esta PC:

```
12:B4:90:05:1C:98:62:53:0B:92:46:51:56:99:26:CC:D3:68:1C:5B
```

**Código:** no hace falta pegar el SHA-1 en el repo; Gradle y Firebase Analytics ya están integrados (`TemplateApp` → `AppAnalytics.init`, plugin `google-services`, `firebase-analytics`).

**JSON:** Analytics funciona con el `google-services.json` actual. Si más adelante activas **Play Games**, vuelve a descargar el JSON desde Firebase (el bloque `oauth_client` de Sudoku se rellenará solo) y reemplaza `app/google-services.json`.

Obtener SHA-1 manualmente (debug):

```powershell
keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

---

## Errores frecuentes

| Síntoma | Causa probable | Solución |
|---------|----------------|----------|
| Error del plugin Google Services al compilar | `package_name` en JSON ≠ `applicationId` | Revisa `app.template.properties` y vuelve a descargar el JSON con el paquete correcto |
| No llegan eventos a Analytics | JSON placeholder aún en el repo | Sustituye `app/google-services.json` por el real |
| Eventos mezclados entre apps | Proyecto compartido sin filtrar | Filtra por `app_slug` en GA4; verifica `appSlug` único por app |
| Firebase pide añadir SDK | Asistente genérico | Ignorar; el SDK ya está en Gradle |
| Dos apps, un JSON confuso | Normal en proyecto compartido | Cada build usa solo su `package_name`; no borres entradas del otro juego |

---

## Checklist rápido

### Proyecto compartido

- [ ] `applicationId` y `appSlug` definidos en `app.template.properties`
- [ ] App Android añadida al Firebase existente con el mismo paquete
- [ ] `app/google-services.json` descargado y en `app/`
- [ ] `project_id` = proyecto compartido (ej. `buscaminas-minesweeper`)
- [ ] Cliente con tu `package_name` presente en el JSON
- [ ] `.\gradlew assembleDebug` sin errores
- [ ] (Opcional) eventos en DebugView con `app_slug` correcto

### Proyecto independiente

- [ ] Proyecto Firebase nuevo creado
- [ ] App Android registrada con `applicationId` correcto
- [ ] `app/google-services.json` en `app/` (un solo cliente)
- [ ] `.\gradlew assembleDebug` sin errores

---

## Referencias en este repo

| Archivo | Rol |
|---------|-----|
| `app.template.properties` | `applicationId`, `appSlug`, `appDisplayName` |
| `app/google-services.json` | Config Firebase (descargado de la consola) |
| `app/build.gradle.kts` | Plugin y dependencias Firebase (no editar para Firebase) |
| `app/src/main/java/.../analytics/AppAnalytics.kt` | Envía `app_slug` en cada evento |

**Siguiente paso del checklist de publicación:** [AdMob](ADMOB.md) — `app/src/main/res/values/admob.xml`.
