# Publicar la política de privacidad (GitHub Pages)

La URL pública debe coincidir con `privacy_policy_url` en `app/src/main/res/values/legal.xml`.

**URL prevista:** https://j-maker.github.io/sudoku/

---

## Paso 1 — Revisar el HTML local

1. Abre `store-assets/privacy.html` en el navegador (doble clic o arrastra al Chrome).
2. Comprueba que dice **Sudoku**, **AdMob**, **Firebase Analytics**, **Play Integrity** y tu email de contacto.
3. Si añades funciones nuevas (estadísticas de partidas, Play Games, etc.), actualiza el HTML antes de publicar.

---

## Paso 2 — Subir a GitHub

Repositorio: **https://github.com/j-maker/sudoku**

### Opción A — GitHub Pages desde la raíz del repo (recomendada)

GitHub Pages busca **`index.html` en la raíz del repositorio**, no dentro de `store-assets/`.

| Archivo | Para qué |
|---------|----------|
| `store-assets/privacy.html` | Fuente de verdad (edítalo aquí) |
| `index.html` (raíz) | Copia que publica GitHub Pages |

**Actualizar la copia de la raíz** (cada vez que cambies la política):

```powershell
cd "C:\Users\WORKSTATION\Desktop\Repositorios\Play Store Main\sudoku"
Copy-Item store-assets\privacy.html index.html -Force
```

**Subir a GitHub:**

```powershell
git add index.html store-assets\privacy.html app\src\main\res\values\legal.xml docs\
git commit -m "Añadir política de privacidad para GitHub Pages"
git push origin main
```

3. GitHub → repo **sudoku** → **Settings** → **Pages**.
4. **Source:** Deploy from a branch.
5. **Branch:** `main` → carpeta **`/ (root)`** → **Save**.
6. Espera 1–5 minutos. La URL será: **https://j-maker.github.io/sudoku/**

### Opción B — Carpeta `docs/` en el repo

1. Copia `privacy.html` a `docs/index.html`.
2. Pages → branch `main` → folder **`/docs`**.

---

## Paso 3 — Verificar la URL pública

1. Abre **https://j-maker.github.io/sudoku/** en el navegador (o la URL que hayas configurado).
2. Debe cargar la política en español, sin error 404.
3. Si GitHub Pages aún no está activo, verás 404 hasta completar el Paso 2.

---

## Paso 4 — Actualizar la app (`legal.xml`)

Archivo: `app/src/main/res/values/legal.xml`

```xml
<string name="privacy_contact_email" translatable="false">jhonatan.diaz2012@gmail.com</string>
<string name="privacy_policy_url" translatable="false">https://j-maker.github.io/sudoku/</string>
```

*(Ya configurado en el repo si seguiste la plantilla actualizada.)*

Reinstala la app y prueba: **Ajustes → Política de privacidad** debe abrir esa URL.

---

## Paso 5 — Play Console (al crear la ficha)

1. [Play Console](https://play.google.com/console) → tu app Sudoku.
2. **Contenido de la app** → **Política de privacidad**.
3. Pega la misma URL: `https://j-maker.github.io/sudoku/`

---

## Paso 6 — AdMob UMP (consentimiento GDPR)

> **PENDIENTE:** mismo flujo para **Sudoku** y **Buscaminas** (mensajes y apps vinculadas por separado en AdMob).

| App | URL en el mensaje GDPR |
|-----|------------------------|
| Sudoku | https://j-maker.github.io/sudoku/ |
| Buscaminas | https://j-maker.github.io/busccaminas-arcade/ |

1. [AdMob](https://apps.admob.com/) → **Privacidad y mensajes** → **Reglamentos europeos**.
2. Crea o edita el mensaje GDPR (recomendado: **un mensaje por app** con su URL).
3. **Tus aplicaciones:** vincula la app correspondiente (`com.jmaker.sudoku` o `com.jmaker.buscaminasarcade`).
4. Opciones: Consentimiento, Gestionar opciones y **No consentir** activados; **Cerrar (no consentir)** activado.
5. **Publicar** el mensaje.

Para Sudoku, enlace: **https://j-maker.github.io/sudoku/**

Ver también: [ADMOB.md](ADMOB.md) — Paso 9.

---

## Checklist

- [ ] `store-assets/privacy.html` revisado (Sudoku, AdMob, Firebase, contacto)
- [ ] `index.html` en GitHub (o Pages desde `/docs`)
- [ ] GitHub Pages activado en Settings → Pages
- [ ] URL pública carga sin 404
- [ ] `legal.xml` con la misma URL y email
- [ ] App: Ajustes → Política de privacidad abre la URL
- [ ] Play Console: URL pegada (cuando exista la ficha)
- [ ] AdMob UMP (Reglamentos europeos): mensaje publicado y app vinculada — **Sudoku** y **Buscaminas** (pendiente)

---

## Errores frecuentes

| Problema | Solución |
|----------|----------|
| 404 en GitHub Pages | Activa Pages en Settings; espera unos minutos; revisa que exista `index.html` |
| URL distinta en app y Play | Usa exactamente la misma en `legal.xml`, Play Console y AdMob |
| Página en blanco | Sube `index.html` con el contenido de `privacy.html`, no solo el archivo en `store-assets/` |

---

## Relacionado

| Archivo | Rol |
|---------|-----|
| `store-assets/privacy.html` | Fuente de verdad del texto |
| `app/src/main/res/values/legal.xml` | URL y email en la app |
| `docs/ADMOB.md` | UMP y consentimiento |
