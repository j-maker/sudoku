package com.template.android.ui.sudoku

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import com.template.android.ui.layout.SudokuLayoutInfo
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.template.android.R
import com.template.android.ui.theme.SudokuAccent
import com.template.android.ui.theme.SudokuNeonGreen
import com.template.android.ui.theme.SudokuNeonPink
import com.template.android.ui.theme.SudokuPrimary

private val ChromeCaptionStyle = TextStyle(
    platformStyle = PlatformTextStyle(includeFontPadding = false),
)

@Composable
fun SudokuGameHeader(
    audioEnabled: Boolean,
    consecutiveDays: Int,
    layout: SudokuLayoutInfo,
    onOpenShare: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleAudio: () -> Unit,
    headerIconSize: Dp = 40.dp,
    headerSideWidth: Dp = 54.dp,
    titleFontSize: TextUnit = 26.sp,
    subtitleFontSize: TextUnit = 11.sp,
    headerIconContentSize: Dp = 22.dp,
    verticalPadding: Dp = 6.dp,
    modifier: Modifier = Modifier,
) {
    val shareLabel = stringResource(R.string.share_title)
    val statsLabel = stringResource(R.string.stats_title)
    val settingsLabel = stringResource(R.string.settings_title)
    val audioLabel = stringResource(
        if (audioEnabled) R.string.a11y_sound_off else R.string.a11y_sound_on,
    )
    val titleText = stringResource(R.string.game_title).uppercase()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = verticalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.width(headerSideWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            HeaderIconButton(
                onClick = onOpenShare,
                contentDescription = shareLabel,
                size = headerIconSize,
                accent = SudokuPrimary,
            ) {
                HeaderGlyph(Icons.Filled.Share, headerIconContentSize, SudokuPrimary)
            }
            HeaderIconButton(
                onClick = onOpenStats,
                contentDescription = statsLabel,
                size = headerIconSize,
                accent = SudokuAccent,
            ) {
                HeaderGlyph(Icons.Filled.BarChart, headerIconContentSize, SudokuAccent)
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = titleText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    lineHeight = titleFontSize * 1.15f,
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
            Text(
                text = stringResource(R.string.game_subtitle),
                color = SudokuAccent,
                style = ChromeCaptionStyle.copy(
                    fontSize = subtitleFontSize,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    lineHeight = subtitleFontSize * 1.25f,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            )
            DailyStreakChip(
                consecutiveDays = consecutiveDays,
                layout = layout,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Column(
            modifier = Modifier.width(headerSideWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            HeaderIconButton(
                onClick = onOpenSettings,
                contentDescription = settingsLabel,
                size = headerIconSize,
                accent = SudokuAccent,
            ) {
                HeaderGlyph(Icons.Filled.Settings, headerIconContentSize, SudokuAccent)
            }
            HeaderIconButton(
                onClick = onToggleAudio,
                contentDescription = audioLabel,
                size = headerIconSize,
                accent = if (audioEnabled) SudokuNeonGreen else SudokuNeonPink,
            ) {
                HeaderGlyph(
                    imageVector = if (audioEnabled) {
                        Icons.AutoMirrored.Filled.VolumeUp
                    } else {
                        Icons.AutoMirrored.Filled.VolumeOff
                    },
                    size = headerIconContentSize,
                    tint = if (audioEnabled) SudokuNeonGreen else SudokuNeonPink,
                )
            }
        }
    }
}

@Composable
private fun DailyStreakChip(
    consecutiveDays: Int,
    layout: SudokuLayoutInfo,
    modifier: Modifier = Modifier,
) {
    val streakValue = if (consecutiveDays == 1) {
        stringResource(R.string.streak_day, consecutiveDays)
    } else {
        stringResource(R.string.streak_days, consecutiveDays)
    }
    val captionSize = (layout.subtitleFontSize.value * 0.95f).sp

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1038).copy(alpha = 0.9f))
            .border(1.dp, SudokuNeonPink.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.LocalFireDepartment,
            contentDescription = null,
            tint = SudokuNeonPink,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = stringResource(R.string.status_daily_streak_label),
            style = ChromeCaptionStyle.copy(
                fontSize = captionSize,
                fontWeight = FontWeight.SemiBold,
                lineHeight = captionSize * 1.1f,
                color = Color.White.copy(alpha = 0.8f),
            ),
            modifier = Modifier.padding(start = 4.dp, end = 6.dp),
        )
        Text(
            text = streakValue,
            style = ChromeCaptionStyle.copy(
                fontSize = layout.subtitleFontSize,
                fontWeight = FontWeight.Bold,
                lineHeight = layout.subtitleFontSize * 1.1f,
                color = SudokuNeonPink,
            ),
        )
    }
}

@Composable
fun SudokuStatusCard(
    elapsedSeconds: Int,
    mistakes: Int,
    notesMode: Boolean,
    onReset: () -> Unit,
    onToggleNotes: () -> Unit,
    isCompact: Boolean = false,
    chipHeight: Dp = if (isCompact) 54.dp else 58.dp,
    captionFontSize: TextUnit = if (isCompact) 12.sp else 13.sp,
    valueFontSize: TextUnit = if (isCompact) 18.sp else 19.sp,
    modifier: Modifier = Modifier,
) {
    val resetLabel = stringResource(R.string.game_reset)
    val notesBtnLabel = stringResource(R.string.status_notes_btn)
    val notesStateLabel = if (notesMode) {
        stringResource(R.string.status_notes_on)
    } else {
        stringResource(R.string.status_notes_off)
    }
    val notesContentDescription = "$notesBtnLabel — $notesStateLabel"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF140830).copy(alpha = 0.92f))
            .border(1.dp, SudokuPrimary.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusCounterChip(
            icon = Icons.Filled.Schedule,
            caption = stringResource(R.string.status_time_label),
            value = formatTime(elapsedSeconds),
            valueColor = SudokuPrimary,
            height = chipHeight,
            captionFontSize = captionFontSize,
            valueFontSize = valueFontSize,
            modifier = Modifier.weight(1f),
        )
        StatusCounterChip(
            icon = Icons.Filled.Close,
            caption = stringResource(R.string.status_mistakes_label),
            value = mistakes.toString(),
            valueColor = if (mistakes > 0) SudokuNeonPink else SudokuNeonGreen,
            height = chipHeight,
            captionFontSize = captionFontSize,
            valueFontSize = valueFontSize,
            modifier = Modifier.weight(1f),
        )
        StatusActionChip(
            icon = Icons.Filled.Refresh,
            caption = resetLabel,
            accent = SudokuAccent,
            active = false,
            height = chipHeight,
            captionFontSize = captionFontSize,
            onClick = onReset,
            modifier = Modifier.weight(0.9f),
        )
        StatusActionChip(
            icon = Icons.Filled.Edit,
            caption = if (notesMode) notesStateLabel else notesBtnLabel,
            accent = SudokuPrimary,
            active = notesMode,
            height = chipHeight,
            captionFontSize = captionFontSize,
            contentDescription = notesContentDescription,
            onClick = onToggleNotes,
            modifier = Modifier.weight(0.9f),
        )
    }
}

@Composable
private fun HeaderGlyph(
    imageVector: ImageVector,
    size: Dp,
    tint: Color,
) {
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(size),
    )
}

@Composable
private fun StatusCounterChip(
    icon: ImageVector,
    caption: String,
    value: String,
    valueColor: Color,
    height: Dp,
    captionFontSize: TextUnit,
    valueFontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .defaultMinSize(minHeight = height)
            .heightIn(min = height)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0D0820))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.72f),
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = caption,
                style = ChromeCaptionStyle.copy(
                    fontSize = captionFontSize,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = captionFontSize * 1.3f,
                    color = Color.White.copy(alpha = 0.78f),
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = value,
            style = ChromeCaptionStyle.copy(
                fontSize = valueFontSize,
                fontWeight = FontWeight.Bold,
                lineHeight = valueFontSize * 1.15f,
                color = valueColor,
            ),
            maxLines = 1,
        )
    }
}

@Composable
private fun StatusActionChip(
    icon: ImageVector,
    caption: String,
    accent: Color,
    active: Boolean,
    height: Dp,
    captionFontSize: TextUnit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    Column(
        modifier = modifier
            .defaultMinSize(minHeight = height)
            .heightIn(min = height)
            .semantics { this.contentDescription = contentDescription ?: caption }
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (active) accent.copy(alpha = 0.22f) else Color(0xFF0D0820),
            )
            .border(
                1.dp,
                accent.copy(alpha = if (active) 0.75f else 0.35f),
                RoundedCornerShape(8.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = caption,
            style = ChromeCaptionStyle.copy(
                fontSize = captionFontSize,
                fontWeight = FontWeight.Bold,
                lineHeight = captionFontSize * 1.3f,
                color = if (active) accent else Color.White.copy(alpha = 0.85f),
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun HeaderIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    size: Dp = 40.dp,
    accent: Color = SudokuPrimary,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.verticalGradient(listOf(Color(0xFF2A1458), Color(0xFF120828))),
            )
            .border(1.5.dp, accent.copy(alpha = 0.85f), RoundedCornerShape(10.dp))
            .semantics { this.contentDescription = contentDescription }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
