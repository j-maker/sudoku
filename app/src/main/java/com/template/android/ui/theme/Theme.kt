package com.template.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.template.android.ui.layout.CappedFontScale

private val SudokuDarkColorScheme = darkColorScheme(
    primary = SudokuPrimary,
    onPrimary = Color(0xFF001A22),
    primaryContainer = Color(0xFF006978),
    onPrimaryContainer = Color(0xFFB2EBFF),
    secondary = SudokuAccent,
    onSecondary = Color(0xFF2A2000),
    tertiary = SudokuNeonPink,
    background = SudokuBackgroundStart,
    onBackground = Color(0xFFF5F8FF),
    surface = SudokuChromeBg,
    onSurface = Color(0xFFF0F4FF),
    surfaceVariant = Color(0xFF2A1858),
    onSurfaceVariant = Color(0xFFB8C5E8),
    outline = SudokuPrimary.copy(alpha = 0.7f),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SudokuDarkColorScheme,
        typography = Typography,
    ) {
        CappedFontScale {
            content()
        }
    }
}
