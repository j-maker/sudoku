package com.template.android.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.template.android.R
import com.template.android.ui.theme.SudokuPrimary
import kotlinx.coroutines.delay

private val SplashTitleStyle = TextStyle(
    platformStyle = PlatformTextStyle(includeFontPadding = false),
)

private val SplashIconShape = RoundedCornerShape(28.dp)

@Composable
fun AppSplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        delay(2_000)
        onFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C1628)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_full),
                contentDescription = null,
                modifier = Modifier
                    .size(160.dp)
                    .clip(SplashIconShape)
                    .border(1.5.dp, SudokuPrimary.copy(alpha = 0.45f), SplashIconShape),
                contentScale = ContentScale.Crop,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = SplashTitleStyle.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                ),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.game_subtitle),
                style = SplashTitleStyle.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = SudokuPrimary.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}
