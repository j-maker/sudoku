package com.template.android.ui.sudoku

import android.content.res.Resources
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.template.android.R
import com.template.android.data.AchievementId
import com.template.android.data.DailyChallengeInfo
import com.template.android.data.DailyChallengeRules
import com.template.android.ui.layout.SudokuLayoutInfo
import com.template.android.ui.theme.SudokuAccent
import com.template.android.ui.theme.SudokuNeonGreen
import com.template.android.ui.theme.SudokuNeonPink
import com.template.android.ui.theme.SudokuPrimary
import kotlinx.coroutines.delay

@Composable
fun DailyChallengeCard(
    info: DailyChallengeInfo,
    layout: SudokuLayoutInfo,
    consecutiveDays: Int = 0,
    modifier: Modifier = Modifier,
    onTap: (() -> Unit)? = null,
) {
    val accent = if (info.completed) SudokuNeonGreen else SudokuNeonPink
    val challengeText = if (info.completed) {
        stringResource(R.string.stats_daily_done_short)
    } else {
        stringResource(DailyChallengeRules.titleRes(info.type))
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF1A1038), Color(0xFF12082A)),
                ),
            )
            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .then(if (onTap != null) Modifier.clickable(onClick = onTap) else Modifier)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f, fill = true),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.stats_daily),
                color = accent,
                fontWeight = FontWeight.Bold,
                fontSize = layout.statusCaptionFontSize,
            )
            Text(
                text = "·",
                color = Color(0xFF7A6A94),
                fontSize = layout.statusCaptionFontSize,
            )
            Text(
                text = challengeText,
                color = Color(0xFFE8DCFF),
                fontSize = layout.statusCaptionFontSize,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
        }
        if (consecutiveDays > 0) {
            DailyStreakBadge(
                consecutiveDays = consecutiveDays,
                fontSize = layout.statusCaptionFontSize,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
    }
}

@Composable
private fun DailyStreakBadge(
    consecutiveDays: Int,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    val label = if (consecutiveDays == 1) {
        stringResource(R.string.streak_day, consecutiveDays)
    } else {
        stringResource(R.string.streak_days, consecutiveDays)
    }
    Text(
        text = "🔥 $label",
        color = SudokuAccent,
        fontWeight = FontWeight.Bold,
        fontSize = fontSize,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            platformStyle = PlatformTextStyle(includeFontPadding = false),
        ),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF2A1548))
            .border(1.dp, SudokuAccent.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Composable
fun AchievementToastHost(
    messages: List<String>,
    onDismissed: () -> Unit,
) {
    var index by remember { mutableIntStateOf(0) }
    val visible = index < messages.size
    val current = messages.getOrNull(index)

    LaunchedEffect(messages) {
        index = 0
    }

    LaunchedEffect(index, messages) {
        if (!visible) {
            onDismissed()
            return@LaunchedEffect
        }
        delay(2_800L)
        index++
    }

    if (current != null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            EngagementToast(message = current)
        }
    }
}

@Composable
private fun EngagementToast(message: String) {
    Text(
        text = message,
        color = Color.White,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(SudokuPrimary.copy(alpha = 0.35f), SudokuAccent.copy(alpha = 0.25f)),
                ),
            )
            .border(1.dp, SudokuPrimary.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
    )
}

fun buildAchievementToastMessages(
    unlocks: List<AchievementId>,
    beatBestTime: Boolean,
    bestTimeDifficulty: SudokuDifficulty?,
    elapsedSeconds: Int,
    res: Resources,
): List<String> {
    val messages = mutableListOf<String>()
    if (beatBestTime && bestTimeDifficulty != null) {
        val diffName = when (bestTimeDifficulty) {
            SudokuDifficulty.EASY -> res.getString(R.string.difficulty_easy)
            SudokuDifficulty.MEDIUM -> res.getString(R.string.difficulty_medium)
            SudokuDifficulty.HARD -> res.getString(R.string.difficulty_hard)
        }
        messages.add(res.getString(R.string.toast_new_record, diffName, formatTime(elapsedSeconds)))
    }
    unlocks.forEach { id ->
        val title = when (id) {
            AchievementId.FIRST_WIN -> res.getString(R.string.achievement_first_win)
            AchievementId.WINS_10 -> res.getString(R.string.achievement_wins_10)
            AchievementId.WIN_HARD -> res.getString(R.string.achievement_win_hard)
            AchievementId.STREAK_3 -> res.getString(R.string.achievement_streak_3)
            AchievementId.PLAY_7_DAYS -> res.getString(R.string.achievement_play_7_days)
            AchievementId.DAILY_CHALLENGE -> res.getString(R.string.achievement_daily)
        }
        messages.add(res.getString(R.string.toast_achievement_unlocked, title))
    }
    return messages
}

fun shareDifficultyLabel(difficulty: SudokuDifficulty, res: Resources): String = when (difficulty) {
    SudokuDifficulty.EASY -> res.getString(R.string.difficulty_easy)
    SudokuDifficulty.MEDIUM -> res.getString(R.string.difficulty_medium)
    SudokuDifficulty.HARD -> res.getString(R.string.difficulty_hard)
}
