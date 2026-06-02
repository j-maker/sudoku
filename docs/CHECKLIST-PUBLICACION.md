# Checklist de publicación — Sudoku

**Paquete:** `com.jmaker.sudoku`  
**Orden recomendado:** app del juego → firma → UMP → AAB → Play Console.

> **Nota:** La infraestructura (Firebase, AdMob, privacidad) avanza sobre una **plantilla**, pero en este repo el **juego Sudoku ya está implementado**. Aun así, antes de publicar revisa UX, textos y capturas reales.

Marca cada ítem cuando lo completes.

---

## 0. App del juego Sudoku — **IMPLEMENTADO (revisar y pulir)**

| # | Tarea | Archivo / notas |
|---|--------|-----------------|
| 0.1 | Implementar lógica y UI del sudoku | `ui/sudoku/SudokuScreen.kt`, `SudokuBoard.kt`, `SudokuEngine.kt` |
| 0.2 | Partida, dificultades, guardado local | `SudokuGameState.kt`, `AppDataStore` |
| 0.3 | Estadísticas | `StatsDialog`, `PlayerStats` |
| 0.4 | Ajustes completos | `SettingsSheet.kt` |
| 0.5 | Instrucciones una página al inicio | `OnboardingTutorialOverlay.kt` |
| 0.6 | Icono y capturas reales del juego | Icono en `store-assets/icons/` y `res/` — capturas pendientes |
| 0.7 | (Opcional) Play Games | `legal.xml` — pendiente |

- [x] Juego jugable (tablero 9×9, notas, dificultades)
- [x] Estadísticas, música, reset, accesibilidad
- [ ] Probar en dispositivo y ajustar UX
- [x] Icono de app (launcher + Play Store 512)
- [ ] Capturas desde la app real

---

## 1. Identidad y servicios — **HECHO**

| # | Tarea | Estado |
|---|--------|--------|
| 1.1 | `app.template.properties` | Hecho |
| 1.2 | Firebase (`google-services.json`) | Hecho — [FIREBASE.md](FIREBASE.md) |
| 1.3 | Analytics (`app_slug=sudoku`) | Hecho — probado en DebugView |
| 1.4 | AdMob (`admob.xml`) | Hecho — [ADMOB.md](ADMOB.md) |

- [x] Paquete, slug, nombre
- [x] Firebase compartido
- [x] AdMob IDs producción

---

## 2. Privacidad — **HECHO (revisar tras implementar juego)**

| # | Tarea | Guía |
|---|--------|------|
| 2.1 | `store-assets/privacy.html` | Hecho (stats, logros, Play Games) |
| 2.2 | GitHub Pages | Hecho — https://j-maker.github.io/sudoku/ |
| 2.3 | `legal.xml` | Hecho |
| 2.4 | Actualizar política si cambia el juego | Tras bloque 0 |

- [x] URL pública activa
- [x] App: Ajustes → Política de privacidad
- [ ] Revisar `privacy.html` cuando el juego esté listo (datos que guarda)

---

## 3. UMP AdMob (GDPR) — **PENDIENTE**

Configurar UMP para **Sudoku** (mensaje por app recomendado).

| App | Paquete | URL privacidad |
|-----|---------|----------------|
| Sudoku | `com.jmaker.sudoku` | https://j-maker.github.io/sudoku/ |

- [ ] Reglamentos europeos: publicado + app vinculada
- [ ] No consentir + Cerrar (no consentir) activados
- [ ] (Opcional) Normativas EE. UU.

Ver [ADMOB.md](ADMOB.md) — Paso 9 · [PUBLICAR-PRIVACIDAD.md](PUBLICAR-PRIVACIDAD.md) — Paso 6.

---

## 4. Keystore y firma — **PENDIENTE**

| # | Tarea | Referencia |
|---|--------|------------|
| 4.1 | `keystore/release.keystore` + copia de seguridad | Crear y respaldar (NO subir a git) |
| 4.2 | `signing.properties` (no git) | `signing.properties.example` |
| 4.3 | AAB: `.\scripts\build-release-aab.ps1` | `app\build\outputs\bundle\release\app-release.aab` |

- [ ] Keystore creado y respaldado
- [ ] AAB genera sin errores

---

## 5. Play Console — **PENDIENTE**

| # | Tarea |
|---|--------|
| 5.1 | Crear app `com.jmaker.sudoku` |
| 5.2 | Política de privacidad (URL GitHub Pages) |
| 5.3 | Seguridad de los datos |
| 5.4 | Subir AAB — prueba interna |
| 5.5 | Ficha, icono 512 px, capturas, clasificación, países |

- [ ] App creada en Play Console
- [ ] AAB en prueba interna
- [ ] Ficha completa

---

## Resumen visual

```
[x] 0. JUEGO SUDOKU          ← implementado; pulir UX y capturas
[x] 1. Firebase / AdMob / identidad
[x] 2. Privacidad + GitHub Pages
[ ] 3. UMP (Sudoku)
[ ] 4. Keystore + AAB
[ ] 5. Play Console + assets
```

---

## Documentación

| Tema | Archivo |
|------|---------|
| Firebase | [FIREBASE.md](FIREBASE.md) |
| AdMob | [ADMOB.md](ADMOB.md) |
| Privacidad / Pages | [PUBLICAR-PRIVACIDAD.md](PUBLICAR-PRIVACIDAD.md) |
| Checklist (este archivo) | [CHECKLIST-PUBLICACION.md](CHECKLIST-PUBLICACION.md) |
