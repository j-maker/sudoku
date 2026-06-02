package com.template.android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.template.android.R

@Composable
fun ModalCloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    tint: Color = Color.White.copy(alpha = 0.75f),
) {
    Icon(
        imageVector = Icons.Filled.Close,
        contentDescription = stringResource(R.string.a11y_close),
        tint = tint,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
    )
}
