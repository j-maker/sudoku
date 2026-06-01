package com.template.android.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.template.android.R
import com.template.android.ads.BannerAdView
import com.template.android.ads.InterstitialAdManager
import com.template.android.analytics.AppAnalytics

@Composable
fun TemplateHomeScreen(
    interstitialAdManager: InterstitialAdManager? = null,
    onOpenSettings: () -> Unit = {},
) {
    val activity = LocalContext.current as? ComponentActivity

    Box(modifier = Modifier.fillMaxSize()) {
        TextButton(
            onClick = {
                AppAnalytics.logButtonClick("open_settings")
                onOpenSettings()
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
        ) {
            Text(stringResource(R.string.home_open_settings))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.home_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp, bottom = 24.dp),
            )
            if (activity != null && interstitialAdManager != null) {
                Button(
                    onClick = {
                        AppAnalytics.logButtonClick("test_interstitial")
                        interstitialAdManager.showIfReady(activity) {
                            interstitialAdManager.preload()
                        }
                    },
                ) {
                    Text(stringResource(R.string.home_test_interstitial))
                }
            }
        }

        BannerAdView(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        )
    }
}
