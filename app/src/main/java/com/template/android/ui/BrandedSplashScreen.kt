package com.template.android.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.template.android.R
import com.template.android.ui.theme.SplashBackgroundCenter
import com.template.android.ui.theme.SplashBackgroundEdge
import com.template.android.ui.theme.SplashBackgroundMid
import com.template.android.ui.theme.SplashGlowCyan
import com.template.android.ui.theme.SudokuAccent
import com.template.android.ui.theme.SudokuNeonPink
import com.template.android.ui.theme.SudokuPrimary

private val SplashIconShape = RoundedCornerShape(28.dp)

@Composable
fun BrandedSplashScreen(modifier: Modifier = Modifier) {
    val maxGameIconWidth = (LocalConfiguration.current.screenWidthDp * 0.48f).dp
    val titleText = stringResource(R.string.splash_title).uppercase()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    0f to SplashBackgroundCenter,
                    0.55f to SplashBackgroundMid,
                    1f to SplashBackgroundEdge,
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 28.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(maxGameIconWidth * 1.15f)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    SplashGlowCyan.copy(alpha = 0.28f),
                                    SplashGlowCyan.copy(alpha = 0.08f),
                                    Color.Transparent,
                                ),
                            ),
                            shape = CircleShape,
                        ),
                )
                Image(
                    painter = painterResource(R.drawable.ic_launcher_full),
                    contentDescription = titleText,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .widthIn(max = maxGameIconWidth)
                        .aspectRatio(1f)
                        .clip(SplashIconShape)
                        .border(1.5.dp, SudokuPrimary.copy(alpha = 0.45f), SplashIconShape),
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = titleText,
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    lineHeight = 36.sp,
                    textAlign = TextAlign.Center,
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    brush = Brush.linearGradient(
                        colors = listOf(SudokuPrimary, SudokuAccent, SudokuNeonPink),
                    ),
                    shadow = Shadow(
                        color = SudokuPrimary.copy(alpha = 0.85f),
                        offset = Offset(0f, 0f),
                        blurRadius = 20f,
                    ),
                ),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.splash_tagline).uppercase(),
                color = SudokuNeonPink.copy(alpha = 0.75f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}
