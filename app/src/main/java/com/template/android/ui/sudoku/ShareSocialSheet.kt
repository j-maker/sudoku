package com.template.android.ui.sudoku

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.template.android.R
import com.template.android.ui.components.ModalHeaderActions
import com.template.android.ui.theme.SudokuPrimary

private data class ShareTarget(
    val platform: SocialPlatform,
    val label: String,
    @DrawableRes val iconRes: Int,
    val color: Color,
)

private data class ShareSheetLayout(
    val buttonSize: Dp,
    val iconSize: Dp,
    val labelFontSize: TextUnit,
    val itemSpacing: Dp,
    val panelPaddingH: Dp,
    val panelPaddingV: Dp,
    val cornerRadius: Dp,
    val topPadding: Dp,
    val endPadding: Dp,
    val maxItemsPerRow: Int,
    val sheetMaxWidth: Dp,
)

@Composable
private fun rememberShareSheetLayout(
    maxWidth: Dp,
    maxHeight: Dp,
    headerIconSize: Dp,
    screenHorizontalPadding: Dp,
): ShareSheetLayout {
    val isNarrow = maxWidth < 360.dp
    val isVeryNarrow = maxWidth < 320.dp
    val isShort = maxHeight < 700.dp

    val buttonSize = when {
        isVeryNarrow -> 40.dp
        isNarrow -> 44.dp
        else -> 48.dp
    }
    val iconSize = buttonSize * 0.54f
    val labelFontSize = when {
        isVeryNarrow -> 8.sp
        isNarrow -> 8.5.sp
        else -> 9.sp
    }
    val itemSpacing = when {
        isVeryNarrow -> 5.dp
        isNarrow -> 7.dp
        else -> 10.dp
    }
    val endPadding = if (isNarrow) screenHorizontalPadding else 8.dp
    val topPadding = when {
        isShort -> headerIconSize + 36.dp
        isNarrow -> headerIconSize + 40.dp
        else -> headerIconSize + 44.dp
    }
    val maxItemsPerRow = when {
        isVeryNarrow -> 3
        isNarrow -> 4
        maxWidth < 400.dp -> 5
        else -> 6
    }
    val sheetMaxWidth = maxWidth - endPadding - screenHorizontalPadding.coerceAtMost(12.dp)

    return ShareSheetLayout(
        buttonSize = buttonSize,
        iconSize = iconSize,
        labelFontSize = labelFontSize,
        itemSpacing = itemSpacing,
        panelPaddingH = if (isNarrow) 8.dp else 12.dp,
        panelPaddingV = if (isNarrow) 8.dp else 10.dp,
        cornerRadius = if (isNarrow) 16.dp else 20.dp,
        topPadding = topPadding,
        endPadding = endPadding,
        maxItemsPerRow = maxItemsPerRow,
        sheetMaxWidth = sheetMaxWidth,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShareSocialSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onShare: (SocialPlatform) -> Unit,
    headerIconSize: Dp = 40.dp,
    screenHorizontalPadding: Dp = 14.dp,
) {
    if (!visible) return

    val targets = listOf(
        ShareTarget(
            platform = SocialPlatform.MORE,
            label = stringResource(R.string.share_more),
            iconRes = R.drawable.ic_more_vert,
            color = Color(0xFF3D5A80),
        ),
        ShareTarget(SocialPlatform.TWITTER, stringResource(R.string.share_twitter), R.drawable.ic_brand_x, Color(0xFF000000)),
        ShareTarget(SocialPlatform.TELEGRAM, stringResource(R.string.share_telegram), R.drawable.ic_brand_telegram, Color(0xFF0088CC)),
        ShareTarget(SocialPlatform.FACEBOOK, stringResource(R.string.share_facebook), R.drawable.ic_brand_facebook, Color(0xFF1877F2)),
        ShareTarget(SocialPlatform.INSTAGRAM, stringResource(R.string.share_instagram), R.drawable.ic_brand_instagram, Color(0xFFE4405F)),
        ShareTarget(SocialPlatform.WHATSAPP, stringResource(R.string.share_whatsapp), R.drawable.ic_brand_whatsapp, Color(0xFF25D366)),
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(20f),
    ) {
        val layout = rememberShareSheetLayout(
            maxWidth = maxWidth,
            maxHeight = maxHeight,
            headerIconSize = headerIconSize,
            screenHorizontalPadding = screenHorizontalPadding,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x99080018))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss,
                ),
        )

        AnimatedVisibility(
            visible = true,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = layout.topPadding, end = layout.endPadding),
            enter = slideInHorizontally(
                animationSpec = tween(320, easing = FastOutSlowInEasing),
                initialOffsetX = { fullWidth -> fullWidth },
            ) + fadeIn(tween(280)),
            exit = slideOutHorizontally(
                animationSpec = tween(260, easing = FastOutSlowInEasing),
                targetOffsetX = { fullWidth -> fullWidth },
            ) + fadeOut(tween(220)),
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = layout.sheetMaxWidth)
                    .shadow(
                        16.dp,
                        RoundedCornerShape(layout.cornerRadius),
                        ambientColor = SudokuPrimary,
                    )
                    .clip(RoundedCornerShape(layout.cornerRadius))
                    .background(Color(0xFF1A0A3A))
                    .border(
                        1.5.dp,
                        SudokuPrimary.copy(alpha = 0.8f),
                        RoundedCornerShape(layout.cornerRadius),
                    )
                    .padding(
                        horizontal = layout.panelPaddingH,
                        vertical = layout.panelPaddingV,
                    ),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopEnd,
                ) {
                    ModalHeaderActions(onDone = onDismiss)
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(
                        layout.itemSpacing,
                        Alignment.End,
                    ),
                    verticalArrangement = Arrangement.spacedBy(layout.itemSpacing),
                    maxItemsInEachRow = layout.maxItemsPerRow,
                ) {
                    targets.forEach { target ->
                        ShareTargetButton(
                            target = target,
                            layout = layout,
                            onClick = {
                                onShare(target.platform)
                                onDismiss()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShareTargetButton(
    target: ShareTarget,
    layout: ShareSheetLayout,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .semantics { contentDescription = target.label }
            .clickable(onClick = onClick)
            .padding(2.dp),
    ) {
        Box(
            modifier = Modifier
                .size(layout.buttonSize)
                .shadow(8.dp, CircleShape, spotColor = target.color)
                .clip(CircleShape)
                .background(target.color)
                .border(2.dp, Color.White.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(target.iconRes),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(layout.iconSize),
            )
        }
        Text(
            text = target.label,
            color = Color.White,
            style = TextStyle(
                fontSize = layout.labelFontSize,
                fontWeight = FontWeight.Bold,
                lineHeight = layout.labelFontSize * 1.15f,
                textAlign = TextAlign.Center,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
            ),
            modifier = Modifier.padding(top = 3.dp),
            maxLines = 2,
        )
    }
}
