package com.template.android.ui.sudoku

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.template.android.data.AppSettings

private val BoardCornerRadius = 12.dp
private val BoardFrameWidth = 1.5.dp
private val BoardShape = RoundedCornerShape(BoardCornerRadius)

@Composable
fun SudokuBoard(
    state: SudokuGameState,
    settings: AppSettings,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = SudokuAccessibility.boardPalette(settings)
    val cellFontSize = SudokuAccessibility.cellFontSize(settings)
    val noteFontSize = SudokuAccessibility.noteFontSize(settings)
    val gridOutlineWidth = SudokuAccessibility.gridOutlineWidth(settings)
    val gridCornerRadius = BoardCornerRadius - BoardFrameWidth

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(BoardShape)
            .background(palette.frameColor)
            .padding(BoardFrameWidth),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(gridCornerRadius))
                .background(palette.blockA),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                for (row in 0 until 9) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    ) {
                        for (col in 0 until 9) {
                            val index = row * 9 + col
                            SudokuCell(
                                index = index,
                                row = row,
                                col = col,
                                state = state,
                                settings = settings,
                                palette = palette,
                                cellFontSize = cellFontSize,
                                noteFontSize = noteFontSize,
                                onClick = { onCellClick(index) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            GridOverlay(
                modifier = Modifier.fillMaxSize(),
                palette = palette,
                cornerRadius = gridCornerRadius,
                outlineWidth = gridOutlineWidth,
            )
        }
    }
}

@Composable
private fun GridOverlay(
    palette: SudokuBoardPalette,
    cornerRadius: Dp,
    outlineWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val cornerPx = with(density) { cornerRadius.toPx() }
    val outlinePx = with(density) { outlineWidth.toPx() }
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cellW = w / 9f
        val cellH = h / 9f

        for (i in 1 until 9) {
            val stroke = if (i % 3 == 0) 2f else 1f
            val color = if (i % 3 == 0) palette.gridBold else palette.gridThin
            val x = i * cellW
            val y = i * cellH
            drawLine(color, Offset(x, 0f), Offset(x, h), strokeWidth = stroke)
            drawLine(color, Offset(0f, y), Offset(w, y), strokeWidth = stroke)
        }

        // Línea exterior del tablero (azul), redondeada y sin cortar en las esquinas.
        val inset = outlinePx / 2f
        val radius = minOf(cornerPx, minOf(w, h) / 2f) - inset
        drawRoundRect(
            color = palette.gridBold,
            topLeft = Offset(inset, inset),
            size = Size(w - outlinePx, h - outlinePx),
            cornerRadius = CornerRadius(radius, radius),
            style = Stroke(width = outlinePx),
        )
    }
}

@Composable
private fun SudokuCell(
    index: Int,
    row: Int,
    col: Int,
    state: SudokuGameState,
    settings: AppSettings,
    palette: SudokuBoardPalette,
    cellFontSize: androidx.compose.ui.unit.TextUnit,
    noteFontSize: androidx.compose.ui.unit.TextUnit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSelected = state.selectedIndex == index
    val isFixed = state.fixed[index]
    val value = state.userValues[index]
    val isError = settings.highlightErrors && state.isCellError(index)
    val selectedValue = if (state.selectedIndex in 0..80) {
        state.userValues[state.selectedIndex]
    } else {
        0
    }
    val highlightSameNumber = !isSelected &&
        selectedValue != 0 &&
        value != 0 &&
        value == selectedValue
    val sameRowOrCol = state.selectedIndex in 0..80 && (
        state.selectedIndex / 9 == row || state.selectedIndex % 9 == col
        )
    val sameBox = state.selectedIndex in 0..80 && run {
        val sr = state.selectedIndex / 9 / 3
        val sc = state.selectedIndex % 9 / 3
        row / 3 == sr && col / 3 == sc
    }
    val highlightPeer = !isSelected &&
        !highlightSameNumber &&
        state.selectedIndex in 0..80 &&
        (sameRowOrCol || sameBox)
    val blockTint = ((row / 3) + (col / 3)) % 2 == 1

    val bg = when {
        isSelected -> palette.cellSelected
        highlightSameNumber -> palette.cellSameNumber
        highlightPeer -> palette.cellPeer
        blockTint -> palette.blockB
        else -> palette.blockA
    }
    val textColor = when {
        isError -> palette.cellError
        isFixed -> palette.cellFixed
        else -> palette.cellUser
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (value != 0) {
            Text(
                text = value.toString(),
                fontSize = cellFontSize,
                fontWeight = if (isFixed || settings.largeCells) FontWeight.Bold else FontWeight.SemiBold,
                color = textColor,
                textAlign = TextAlign.Center,
            )
        } else if (state.notes[index].isNotEmpty()) {
            NotesGrid(
                notes = state.notes[index],
                fontSize = noteFontSize,
                color = palette.notesColor,
            )
        }
    }
}

@Composable
private fun NotesGrid(
    notes: Set<Int>,
    fontSize: androidx.compose.ui.unit.TextUnit,
    color: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        for (r in 0 until 3) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                for (c in 0 until 3) {
                    val num = r * 3 + c + 1
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (num in notes) {
                            Text(
                                text = num.toString(),
                                fontSize = fontSize,
                                color = color,
                                fontWeight = FontWeight.Medium,
                                lineHeight = fontSize,
                            )
                        }
                    }
                }
            }
        }
    }
}
