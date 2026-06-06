package com.template.android.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

/** Límites de modales (85vw de ancho, hasta 90vh de alto; scroll si el contenido excede ese tope). */
object AppDialogDefaults {

    const val WIDTH_FRACTION = 0.85f
    const val HEIGHT_FRACTION = 0.9f

    val titleBottomPadding: Dp = 4.dp
    val contentBottomPadding: Dp = 8.dp
    val footerTopPadding: Dp = 12.dp

    /** Espacio base reservado para cabecera + botones fuera del área con scroll en AlertDialog. */
    val chromeReservedHeight: Dp = 132.dp

    val containerColor: Color = Color(0xFF12082A)
    val textColor: Color = Color(0xFFE8DCFF)

    enum class Size {
        Settings,
        Content,
    }

    fun titlePadding(): Modifier = Modifier.padding(bottom = titleBottomPadding)

    fun scrollContentPadding(): Modifier = Modifier.padding(bottom = contentBottomPadding)
}

@Composable
fun AppDialogDefaults.maxDialogWidth(): Dp {
    val config = LocalConfiguration.current
    return config.screenWidthDp.dp * AppDialogDefaults.WIDTH_FRACTION
}

@Composable
fun AppDialogDefaults.maxDialogHeight(@Suppress("UNUSED_PARAMETER") size: AppDialogDefaults.Size): Dp {
    val config = LocalConfiguration.current
    return config.screenHeightDp.dp * AppDialogDefaults.HEIGHT_FRACTION
}

@Composable
fun AppDialogDefaults.reservedChromeHeight(): Dp {
    val fontScale = min(LocalConfiguration.current.fontScale, MAX_GAME_FONT_SCALE)
    return (AppDialogDefaults.chromeReservedHeight.value * fontScale.coerceAtLeast(1f))
        .dp
        .coerceIn(AppDialogDefaults.chromeReservedHeight, 240.dp)
}

@Composable
fun AppDialogDefaults.maxScrollHeight(size: AppDialogDefaults.Size): Dp {
    val scrollMax = maxDialogHeight(size) - reservedChromeHeight()
    return maxOf(120.dp, scrollMax)
}

@Composable
fun AppDialogDefaults.surfaceModifier(
    @Suppress("UNUSED_PARAMETER") size: AppDialogDefaults.Size = AppDialogDefaults.Size.Content,
): Modifier {
    return Modifier
        .widthIn(max = maxDialogWidth())
        .fillMaxWidth()
        .heightIn(max = maxDialogHeight(size))
}

/**
 * Modal que se ajusta al contenido hasta [AppDialogDefaults.HEIGHT_FRACTION] de la pantalla.
 * Título y pie fijos; solo la zona central hace scroll si hace falta.
 */
@Composable
fun AppModalScaffold(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    footer: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val maxHeight = AppDialogDefaults.maxDialogHeight(AppDialogDefaults.Size.Content)
    val maxScrollHeight = AppDialogDefaults.maxScrollHeight(AppDialogDefaults.Size.Content)

    Column(
        modifier = modifier
            .widthIn(max = AppDialogDefaults.maxDialogWidth())
            .fillMaxWidth()
            .heightIn(max = maxHeight)
            .background(AppDialogDefaults.containerColor, RoundedCornerShape(20.dp))
            .padding(20.dp),
    ) {
        title()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxScrollHeight)
                .verticalScroll(rememberScrollState())
                .then(AppDialogDefaults.scrollContentPadding()),
            content = content,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppDialogDefaults.footerTopPadding),
            content = { footer() },
        )
    }
}

/**
 * Contenido con altura acotada y scroll; para [AlertDialog] donde el pie va en otro slot.
 */
@Composable
fun AppDialogScrollColumn(
    size: AppDialogDefaults.Size = AppDialogDefaults.Size.Content,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = AppDialogDefaults.maxScrollHeight(size))
            .verticalScroll(rememberScrollState())
            .then(AppDialogDefaults.scrollContentPadding()),
        verticalArrangement = verticalArrangement,
        content = content,
    )
}
