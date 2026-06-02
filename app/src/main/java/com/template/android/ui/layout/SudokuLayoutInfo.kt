package com.template.android.ui.layout

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Stable
data class SudokuLayoutInfo(
    val isCompact: Boolean,
    val isShortScreen: Boolean,
    val isTallScreen: Boolean,
    val horizontalPadding: Dp,
    val sectionSpacing: Dp,
    val boardMaxSize: Dp,
    val bannerWidth: Dp,
    val headerIconSize: Dp,
    val headerSideWidth: Dp,
    val titleFontSize: TextUnit,
    val subtitleFontSize: TextUnit,
    val headerIconContentSize: Dp,
    val statusChipHeight: Dp,
    val statusCaptionFontSize: TextUnit,
    val statusValueFontSize: TextUnit,
    val chipHorizontalPadding: Dp,
    val chipVerticalPadding: Dp,
    val chipFontSize: TextUnit,
    val keyVerticalPadding: Dp,
    val keyFontSize: TextUnit,
    val keySpacing: Dp,
    val numpadHeight: Dp,
    val fixedChromeHeight: Dp,
)

@Composable
fun BoxWithConstraintsScope.rememberSudokuLayoutInfo(
    largeCells: Boolean = false,
): SudokuLayoutInfo =
    remember(maxWidth, maxHeight, largeCells) {
        val boardScale = if (largeCells) 1.12f else 1f
        val isCompact = maxWidth < 360.dp
        val isShortScreen = maxHeight < 700.dp
        val isVeryShortScreen = maxHeight < 600.dp
        val isTallScreen = maxHeight >= 780.dp

        val horizontalPadding = if (isCompact) 10.dp else 14.dp
        val sectionSpacing = when {
            isVeryShortScreen -> 2.dp
            isShortScreen -> 3.dp
            isCompact -> 4.dp
            else -> 5.dp
        }

        val bannerWidth = minOf(maxWidth - horizontalPadding * 2, 320.dp)

        val headerIconSize = when {
            isVeryShortScreen -> 36.dp
            isCompact -> 38.dp
            else -> 40.dp
        }
        val streakRowHeight = if (isCompact) 24.dp else 26.dp
        val headerHeight = when {
            isVeryShortScreen -> 88.dp
            isCompact -> 92.dp
            else -> 98.dp
        } + streakRowHeight
        val statusHeight = when {
            isVeryShortScreen -> 58.dp
            isCompact -> 62.dp
            else -> 66.dp
        }
        val difficultyHeight = if (isCompact) 32.dp else 36.dp
        val numpadHeight = when {
            isVeryShortScreen -> 44.dp
            isCompact -> 48.dp
            else -> 52.dp
        }
        val bannerSlot = 50.dp + sectionSpacing * 2

        val fixedChromeHeight = headerHeight + statusHeight + difficultyHeight +
            numpadHeight + bannerSlot + sectionSpacing * 5

        val maxBoardByWidth = maxWidth - horizontalPadding * 2
        val maxBoardByHeight = (maxHeight - fixedChromeHeight).coerceAtLeast(200.dp)
        val boardCap = when {
            isVeryShortScreen -> 260.dp
            isShortScreen -> 300.dp
            isTallScreen -> 420.dp
            else -> 360.dp
        }
        val boardMaxSize = (minOf(maxBoardByWidth, maxBoardByHeight, boardCap) * boardScale)
            .coerceIn(210.dp, boardCap)

        SudokuLayoutInfo(
            isCompact = isCompact,
            isShortScreen = isShortScreen || isVeryShortScreen,
            isTallScreen = isTallScreen,
            horizontalPadding = horizontalPadding,
            sectionSpacing = sectionSpacing,
            boardMaxSize = boardMaxSize,
            bannerWidth = bannerWidth,
            headerIconSize = headerIconSize,
            headerSideWidth = if (isCompact) 52.dp else 54.dp,
            titleFontSize = when {
                isVeryShortScreen -> 22.sp
                isCompact -> 24.sp
                else -> 26.sp
            },
            subtitleFontSize = when {
                isVeryShortScreen -> 11.sp
                isCompact -> 12.sp
                else -> 13.sp
            },
            headerIconContentSize = if (isCompact) 20.dp else 22.dp,
            statusChipHeight = statusHeight - 16.dp,
            statusCaptionFontSize = if (isCompact) 12.sp else 13.sp,
            statusValueFontSize = if (isCompact) 18.sp else 19.sp,
            chipHorizontalPadding = if (isCompact) 10.dp else 12.dp,
            chipVerticalPadding = if (isCompact) 5.dp else 6.dp,
            chipFontSize = if (isCompact) 12.sp else 13.sp,
            keyFontSize = ((if (isCompact) 15.sp else 16.sp).value * if (largeCells) 1.2f else 1f).sp,
            keyVerticalPadding = when {
                largeCells -> if (isVeryShortScreen) 9.dp else if (isCompact) 10.dp else 11.dp
                isVeryShortScreen -> 7.dp
                isCompact -> 8.dp
                else -> 9.dp
            },
            keySpacing = if (isCompact) 3.dp else 4.dp,
            numpadHeight = numpadHeight,
            fixedChromeHeight = fixedChromeHeight,
        )
    }

fun SudokuLayoutInfo.boardSizeFor(availableHeight: Dp): Dp {
    val belowBoard = numpadHeight + sectionSpacing * 2
    val byHeight = (availableHeight - belowBoard).coerceAtLeast(180.dp)
    return minOf(boardMaxSize, byHeight)
}
