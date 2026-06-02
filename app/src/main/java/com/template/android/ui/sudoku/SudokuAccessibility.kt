package com.template.android.ui.sudoku

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.template.android.data.AppSettings
import com.template.android.ui.theme.SudokuAccent
import com.template.android.ui.theme.SudokuCellError
import com.template.android.ui.theme.SudokuPrimary

data class SudokuBoardPalette(
    val frameColor: Color,
    val blockA: Color,
    val blockB: Color,
    val gridThin: Color,
    val gridBold: Color,
    val cellFixed: Color,
    val cellUser: Color,
    val cellSelected: Color,
    val cellPeer: Color,
    val cellSameNumber: Color,
    val notesColor: Color,
    val cellError: Color,
)

object SudokuAccessibility {

    private val defaultPalette = SudokuBoardPalette(
        frameColor = Color(0xFF0A1228),
        blockA = Color(0xFF1A2540),
        blockB = Color(0xFF152035),
        gridThin = Color(0xFF3D5270),
        gridBold = Color(0xFF6BB3D9),
        cellFixed = Color(0xFF90CAF9),
        cellUser = Color(0xFFFFD54F),
        cellSelected = SudokuPrimary.copy(alpha = 0.35f),
        cellPeer = SudokuPrimary.copy(alpha = 0.12f),
        cellSameNumber = SudokuAccent.copy(alpha = 0.32f),
        notesColor = Color.White,
        cellError = SudokuCellError,
    )

    private val highContrastPalette = SudokuBoardPalette(
        frameColor = Color.White,
        blockA = Color(0xFF000000),
        blockB = Color(0xFF1A1A1A),
        gridThin = Color(0xFFE0E0E0),
        gridBold = Color.White,
        cellFixed = Color.White,
        cellUser = Color(0xFFFFEB3B),
        cellSelected = Color(0xFF00E5FF),
        cellPeer = Color(0xFF80DEEA).copy(alpha = 0.35f),
        cellSameNumber = Color(0xFFFFEB3B).copy(alpha = 0.45f),
        notesColor = Color.White,
        cellError = Color(0xFFFF5252),
    )

    fun boardPalette(settings: AppSettings): SudokuBoardPalette =
        if (settings.highContrast) highContrastPalette else defaultPalette

    fun cellFontSize(settings: AppSettings): TextUnit =
        if (settings.largeCells) 32.sp else 23.sp

    fun noteFontSize(settings: AppSettings): TextUnit =
        if (settings.largeCells) 13.sp else 10.sp

    fun boardSizeScale(settings: AppSettings): Float =
        if (settings.largeCells) 1.12f else 1f

    fun numpadFontScale(settings: AppSettings): Float =
        if (settings.largeCells) 1.2f else 1f

    fun numpadKeyPadding(settings: AppSettings): Dp =
        if (settings.largeCells) 11.dp else 9.dp

    fun gridOutlineWidth(settings: AppSettings): Dp =
        if (settings.highContrast) 3.dp else 2.dp
}
