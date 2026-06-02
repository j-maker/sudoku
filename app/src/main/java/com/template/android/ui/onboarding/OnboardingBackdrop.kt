package com.template.android.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.template.android.ui.theme.SudokuBackgroundEnd
import com.template.android.ui.theme.SudokuBackgroundMid
import com.template.android.ui.theme.SudokuBackgroundStart

/** Fondo opaco a pantalla completa detrás de diálogos de onboarding (no deja ver el juego). */
@Composable
fun OnboardingBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(SudokuBackgroundStart, SudokuBackgroundMid, SudokuBackgroundEnd),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
