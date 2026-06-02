package com.template.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TemplateDarkColorScheme = darkColorScheme(
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

private val TemplateLightColorScheme = lightColorScheme(
    primary = TemplatePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8DEF8),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color.White,
    tertiary = Color(0xFF7D5260),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = TemplatePrimary.copy(alpha = 0.4f),
)

@Composable
fun AppTheme(
    darkMode: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkMode) TemplateDarkColorScheme else TemplateLightColorScheme,
        typography = Typography,
        content = content,
    )
}
