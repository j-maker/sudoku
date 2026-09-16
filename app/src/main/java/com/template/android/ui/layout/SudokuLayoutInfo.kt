package com.template.android.ui.layout

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Stable
data class SudokuLayoutInfo(
    val isCompact: Boolean,
    val isVeryCompact: Boolean,
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
    val scrollStatusBar: Boolean,
    val screenVerticalPadding: Dp,
)

@Composable
fun BoxWithConstraintsScope.rememberSudokuLayoutInfo(
    largeCells: Boolean = false,
): SudokuLayoutInfo {
    val systemFontScale = LocalConfiguration.current.fontScale
    return remember(maxWidth, maxHeight, largeCells, systemFontScale) {
        val boardScale = if (largeCells) 1.12f else 1f
        val prefersLargeSystemFont = systemFontScale > 1.05f
        val isVeryCompact = maxWidth < 340.dp || (prefersLargeSystemFont && maxWidth < 380.dp)
        val isCompact = maxWidth < 360.dp || (prefersLargeSystemFont && maxWidth < 400.dp)
        val isShortScreen = maxHeight < 700.dp
        val isVeryShortScreen = maxHeight < 600.dp
        val isTallScreen = maxHeight >= 780.dp
        val chromeScale = when {
            isVeryCompact -> 0.88f
            prefersLargeSystemFont -> 0.92f
            else -> 1f
        }

        val horizontalPadding = if (isCompact) 8.dp else 10.dp
        val sectionSpacing = when {
            isVeryShortScreen -> 2.dp
            isShortScreen -> 3.dp
            isCompact -> 4.dp
            isTallScreen -> 8.dp
            else -> 5.dp
        }

        val bannerWidth = minOf(maxWidth - horizontalPadding * 2, 320.dp)

        val headerIconSize = when {
            isVeryShortScreen -> 36.dp
            isCompact -> 38.dp
            else -> 40.dp
        }
        val headerHeight = when {
            isVeryShortScreen -> 48.dp
            isCompact -> 52.dp
            else -> 56.dp
        }
        val statusHeight = ((when {
            isVeryShortScreen -> 50.dp
            isCompact -> 54.dp
            else -> 58.dp
        }.value * chromeScale).dp).coerceAtLeast(48.dp)
        val difficultyHeight = if (isCompact) 32.dp else 36.dp
        val numpadHeight = when {
            isVeryShortScreen -> 48.dp
            isCompact -> 52.dp
            else -> 56.dp
        }
        val bannerTopGap = 8.dp
        val bannerSlot = 50.dp + bannerTopGap + sectionSpacing

        val fixedChromeHeight = headerHeight + statusHeight + difficultyHeight +
            numpadHeight + bannerSlot + sectionSpacing * 5

        val maxBoardByWidth = maxWidth - horizontalPadding * 2
        val boardMaxSize = maxBoardByWidth * boardScale

        val chipFontSize = when {
            prefersLargeSystemFont && isCompact -> 11.sp
            isCompact -> 12.sp
            else -> 13.sp
        }

        SudokuLayoutInfo(
            isCompact = isCompact,
            isVeryCompact = isVeryCompact,
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
            statusChipHeight = statusHeight - 10.dp,
            statusCaptionFontSize = when {
                prefersLargeSystemFont && isCompact -> 10.sp
                isCompact -> 11.sp
                else -> 12.sp
            },
            statusValueFontSize = when {
                prefersLargeSystemFont && isCompact -> 16.sp
                isCompact -> 17.sp
                else -> 18.sp
            },
            chipHorizontalPadding = if (isCompact) 10.dp else 12.dp,
            chipVerticalPadding = if (isCompact) 5.dp else 6.dp,
            chipFontSize = chipFontSize,
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
            scrollStatusBar = isCompact || isVeryCompact || prefersLargeSystemFont,
            screenVerticalPadding = when {
                isVeryShortScreen -> 4.dp
                isShortScreen -> 6.dp
                isTallScreen -> 12.dp
                else -> 8.dp
            },
        )
    }
}

/** Tope de altura del tablero en pantallas altas con distribución vertical. */
fun SudokuLayoutInfo.distributedBoardMaxHeight(screenHeight: Dp): Dp {
    val reserved = screenHeight * when {
        isTallScreen -> 0.50f
        else -> 0.52f
    }
    return (screenHeight - reserved).coerceIn(200.dp, 460.dp)
}

fun SudokuLayoutInfo.boardSizeFor(availableHeight: Dp): Dp {
    val belowBoard = numpadHeight + sectionSpacing * 2
    val byHeight = (availableHeight - belowBoard).coerceAtLeast(180.dp)
    return minOf(boardMaxSize, byHeight)
}
