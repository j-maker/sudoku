package com.template.android.ui.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import kotlin.math.min

/** Máximo escalado de letra del sistema que respeta el layout fijo del juego. */
const val MAX_GAME_FONT_SCALE = 1.15f

/**
 * Limita el escalado de letra del sistema para que los contenedores fijos (tablero, barra de
 * estado, teclado) no se desborden. La accesibilidad visual sigue disponible con "Celdas grandes"
 * en Ajustes.
 */
@Composable
fun CappedFontScale(
    maxScale: Float = MAX_GAME_FONT_SCALE,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val cappedScale = min(LocalConfiguration.current.fontScale, maxScale)
    if (density.fontScale != cappedScale) {
        CompositionLocalProvider(
            LocalDensity provides Density(density.density, cappedScale),
        ) {
            content()
        }
    } else {
        content()
    }
}

/** Contenido con tamaño fijo (iconos en botones de tamaño dp). */
@Composable
fun NonScalingContent(content: @Composable () -> Unit) {
    val density = LocalDensity.current
    if (density.fontScale != 1f) {
        CompositionLocalProvider(
            LocalDensity provides Density(density.density, fontScale = 1f),
        ) {
            content()
        }
    } else {
        content()
    }
}
