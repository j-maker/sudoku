package com.template.android.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.template.android.R
import com.template.android.analytics.AppAnalytics
import com.template.android.ui.theme.SudokuAccent
import com.template.android.ui.theme.SudokuNeonPink
import com.template.android.ui.theme.SudokuPrimary

private const val TARGET_PACKAGE = "com.jmaker.buscaminasarcade"
private val BannerRowHeight = 50.dp

@Composable
fun MoreGamesFab(
    animate: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = tween(if (animate) 220 else 0),
        label = "moreGamesPlus",
    )
    val openLabel = stringResource(R.string.a11y_more_games_open)
    val closeLabel = stringResource(R.string.a11y_more_games_close)
    val enter = if (animate) fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.86f) else fadeIn(tween(0))
    val exit = if (animate) fadeOut(tween(140)) + scaleOut(tween(140), targetScale = 0.86f) else fadeOut(tween(0))

    BackHandler(enabled = expanded) { expanded = false }

    Box(modifier = modifier.fillMaxSize()) {
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { expanded = false },
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 8.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AnimatedVisibility(
                visible = expanded,
                enter = enter,
                exit = exit,
            ) {
                MoreGamesCard(
                    animate = animate,
                    onOpenStore = {
                        AppAnalytics.logMoreGamesStore(TARGET_PACKAGE)
                        expanded = false
                        openPlayStoreListing(context, TARGET_PACKAGE)
                    },
                )
            }
            Box(
                modifier = Modifier.height(BannerRowHeight),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(6.dp, CircleShape, ambientColor = SudokuAccent, spotColor = SudokuPrimary)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF1A3A66), Color(0xFF0A1829))),
                        )
                        .border(1.dp, SudokuPrimary.copy(alpha = 0.85f), CircleShape)
                        .semantics { contentDescription = if (expanded) closeLabel else openLabel }
                        .clickable {
                            expanded = !expanded
                            if (expanded) AppAnalytics.logMoreGamesOpen()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "+",
                        color = SudokuPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.rotate(rotation),
                    )
                }
            }
        }
    }
}

@Composable
private fun MoreGamesCard(
    animate: Boolean,
    onOpenStore: () -> Unit,
) {
    val title = stringResource(R.string.more_games_title)
    val gameName = stringResource(R.string.more_games_target_name)
    val glowPulse = rememberInfiniteTransition(label = "moreGamesGlow")
    val glowAlpha by glowPulse.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "moreGamesGlowAlpha",
    )
    val glow = if (animate) glowAlpha else 0.85f
    val shape = RoundedCornerShape(16.dp)
    val annotated = buildAnnotatedString {
        withLink(
            LinkAnnotation.Clickable(
                tag = "play_store",
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = SudokuAccent,
                        fontWeight = FontWeight.Black,
                        textDecoration = TextDecoration.Underline,
                        shadow = Shadow(
                            color = SudokuAccent.copy(alpha = glow),
                            offset = Offset.Zero,
                            blurRadius = 16f,
                        ),
                    ),
                ),
                linkInteractionListener = { onOpenStore() },
            ),
        ) {
            append(gameName)
        }
    }

    Column(
        modifier = Modifier
            .widthIn(max = 260.dp)
            .shadow(16.dp, shape, ambientColor = SudokuNeonPink, spotColor = SudokuPrimary)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF1A3A66), Color(0xFF12082A), Color(0xFF071A3A)),
                ),
            )
            .border(2.dp, SudokuPrimary.copy(alpha = glow), shape)
            .clickable(onClick = onOpenStore)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "🎮 $title",
            color = SudokuNeonPink,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.4.sp,
            style = TextStyle(
                shadow = Shadow(
                    color = SudokuNeonPink.copy(alpha = glow),
                    offset = Offset.Zero,
                    blurRadius = 12f,
                ),
            ),
        )
        Text(
            text = annotated,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            lineHeight = 22.sp,
        )
        Text(
            text = stringResource(R.string.more_games_download) + " →",
            color = SudokuPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            style = TextStyle(
                shadow = Shadow(
                    color = SudokuPrimary.copy(alpha = glow),
                    offset = Offset.Zero,
                    blurRadius = 10f,
                ),
            ),
        )
    }
}

fun openPlayStoreListing(context: Context, packageName: String) {
    val marketUri = Uri.parse("market://details?id=$packageName")
    val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
    val flags = Intent.FLAG_ACTIVITY_NEW_TASK
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, marketUri)
                .setPackage("com.android.vending")
                .addFlags(flags),
        )
    } catch (_: ActivityNotFoundException) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, marketUri).addFlags(flags))
        } catch (_: ActivityNotFoundException) {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, webUri).addFlags(flags))
            } catch (_: ActivityNotFoundException) {
                // Sin Play Store ni navegador
            }
        }
    }
}
