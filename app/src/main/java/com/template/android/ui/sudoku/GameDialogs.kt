package com.template.android.ui.sudoku

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.template.android.R
import com.template.android.data.DailyChallengeInfo
import com.template.android.data.DailyChallengeRules
import com.template.android.data.PlayerStats
import com.template.android.ui.components.ModalHeaderActions
import com.template.android.ui.components.ModalHeaderActionsLight
import com.template.android.ui.layout.AppDialogDefaults
import com.template.android.ui.layout.AppDialogScrollColumn
import com.template.android.ui.layout.AppModalScaffold
import com.template.android.ui.layout.surfaceModifier
import com.template.android.ui.theme.SudokuAccent
import com.template.android.ui.theme.SudokuNeonGreen
import com.template.android.ui.theme.SudokuNeonPink
import com.template.android.ui.theme.SudokuPrimary

private val DialogTextStyle = TextStyle(
    platformStyle = PlatformTextStyle(includeFontPadding = false),
)

private val StatsCardShape = RoundedCornerShape(14.dp)
private val StatsDialogShape = RoundedCornerShape(22.dp)

@Composable
fun SudokuStyledDialog(
    icon: ImageVector,
    iconAccent: Color,
    title: String,
    onDismiss: () -> Unit,
    size: AppDialogDefaults.Size = AppDialogDefaults.Size.Content,
    borderColors: List<Color> = listOf(SudokuPrimary, SudokuNeonPink, SudokuAccent),
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .then(AppDialogDefaults.surfaceModifier(size))
                .clip(StatsDialogShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1A1038), Color(0xFF12082A)),
                    ),
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(borderColors),
                    shape = StatsDialogShape,
                )
                .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconAccent.copy(alpha = 0.18f))
                        .border(1.dp, iconAccent.copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconAccent,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Text(
                    text = title,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    style = DialogTextStyle.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    ),
                )
                ModalHeaderActions(onDone = onDismiss)
            }
            content()
        }
    }
}

@Composable
fun StatsDialog(
    stats: PlayerStats,
    dailyInfo: DailyChallengeInfo = DailyChallengeRules.infoFor(stats),
    onDismiss: () -> Unit,
) {
    val winRate = if (stats.gamesPlayed > 0) {
        (stats.gamesWon * 100) / stats.gamesPlayed
    } else {
        0
    }
    val dailyStreakLabel = if (stats.consecutiveDays == 1) {
        stringResource(R.string.streak_day, stats.consecutiveDays)
    } else {
        stringResource(R.string.streak_days, stats.consecutiveDays)
    }
    val noTime = stringResource(R.string.stats_no_time)

    SudokuStyledDialog(
        icon = Icons.Filled.BarChart,
        iconAccent = SudokuPrimary,
        title = stringResource(R.string.stats_title),
        onDismiss = onDismiss,
    ) {
        AppDialogScrollColumn(
            size = AppDialogDefaults.Size.Content,
            modifier = Modifier.padding(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            StatsSectionTitle(stringResource(R.string.stats_daily))
            Spacer(modifier = Modifier.height(10.dp))
            StatsMetricCard(
                label = stringResource(R.string.stats_daily),
                value = if (dailyInfo.completed) {
                    stringResource(R.string.stats_daily_done)
                } else {
                    stringResource(DailyChallengeRules.titleRes(dailyInfo.type))
                },
                accent = if (dailyInfo.completed) SudokuNeonGreen else SudokuNeonPink,
                icon = Icons.Filled.EmojiEvents,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(18.dp))
            StatsSectionTitle(stringResource(R.string.stats_section_progress))
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatsMetricCard(
                    label = stringResource(R.string.stats_label_won),
                    value = stats.gamesWon.toString(),
                    accent = SudokuNeonGreen,
                    icon = Icons.Filled.EmojiEvents,
                    modifier = Modifier.weight(1f),
                )
                StatsMetricCard(
                    label = stringResource(R.string.stats_label_played),
                    value = stats.gamesPlayed.toString(),
                    accent = SudokuPrimary,
                    icon = Icons.Filled.SportsEsports,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            StatsMetricCard(
                label = stringResource(R.string.stats_label_win_rate),
                value = stringResource(R.string.stats_win_rate_value, winRate),
                accent = SudokuAccent,
                icon = Icons.Filled.BarChart,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(18.dp))
            StatsSectionTitle(stringResource(R.string.stats_section_streaks))
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatsMetricCard(
                    label = stringResource(R.string.stats_label_current_streak),
                    value = stats.winStreak.toString(),
                    accent = SudokuPrimary,
                    icon = Icons.Filled.Whatshot,
                    modifier = Modifier.weight(1f),
                )
                StatsMetricCard(
                    label = stringResource(R.string.stats_label_best_streak),
                    value = stats.bestWinStreak.toString(),
                    accent = SudokuNeonPink,
                    icon = Icons.Filled.EmojiEvents,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            StatsMetricCard(
                label = stringResource(R.string.stats_label_daily_streak),
                value = dailyStreakLabel,
                accent = SudokuNeonPink,
                icon = Icons.Filled.LocalFireDepartment,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(18.dp))
            StatsSectionTitle(stringResource(R.string.stats_section_records))
            Spacer(modifier = Modifier.height(10.dp))
            StatsTimeRow(
                label = stringResource(R.string.difficulty_easy),
                time = stats.bestTimeEasy?.let { formatTime(it) } ?: noTime,
                accent = SudokuPrimary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            StatsTimeRow(
                label = stringResource(R.string.difficulty_medium),
                time = stats.bestTimeMedium?.let { formatTime(it) } ?: noTime,
                accent = SudokuAccent,
            )
            Spacer(modifier = Modifier.height(8.dp))
            StatsTimeRow(
                label = stringResource(R.string.difficulty_hard),
                time = stats.bestTimeHard?.let { formatTime(it) } ?: noTime,
                accent = SudokuNeonPink,
            )

        }
    }
}

@Composable
private fun StatsSectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        style = DialogTextStyle.copy(
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
            color = Color.White.copy(alpha = 0.55f),
        ),
    )
}

@Composable
private fun StatsMetricCard(
    label: String,
    value: String,
    accent: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(StatsCardShape)
            .background(Color(0xFF0E0820).copy(alpha = 0.85f))
            .border(1.dp, accent.copy(alpha = 0.35f), StatsCardShape)
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = DialogTextStyle.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.65f),
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = DialogTextStyle.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
            ),
            textAlign = TextAlign.Start,
        )
    }
}

@Composable
private fun StatsTimeRow(
    label: String,
    time: String,
    accent: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(StatsCardShape)
            .background(Color(0xFF0E0820).copy(alpha = 0.85f))
            .border(1.dp, accent.copy(alpha = 0.35f), StatsCardShape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accent),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = DialogTextStyle.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.85f),
            ),
        )
        Icon(
            imageVector = Icons.Filled.Schedule,
            contentDescription = null,
            tint = accent.copy(alpha = 0.8f),
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = time,
            style = DialogTextStyle.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
            ),
        )
    }
}

@Composable
fun InstructionsDialog(
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        AppModalScaffold(
            title = {
                Text(
                    text = stringResource(R.string.instructions_title),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = AppDialogDefaults.titlePadding(),
                )
            },
            footer = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    ModalHeaderActionsLight(onDone = onDismiss)
                }
            },
            content = {
                Text(
                    text = stringResource(R.string.instructions_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppDialogDefaults.textColor,
                )
            },
        )
    }
}

@Composable
fun WinDialog(
    elapsedSeconds: Int,
    mistakes: Int,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onNewGame: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .then(AppDialogDefaults.surfaceModifier(AppDialogDefaults.Size.Content))
                .clip(StatsDialogShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1A1038), Color(0xFF12082A)),
                    ),
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        listOf(SudokuNeonGreen, SudokuPrimary, SudokuAccent),
                    ),
                    shape = StatsDialogShape,
                )
                .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SudokuNeonGreen.copy(alpha = 0.18f))
                        .border(1.dp, SudokuNeonGreen.copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = SudokuNeonGreen,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Text(
                    text = stringResource(R.string.win_title),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    style = DialogTextStyle.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    ),
                )
                ModalHeaderActions(onDone = onDismiss)
            }

            AppDialogScrollColumn(
                size = AppDialogDefaults.Size.Content,
                modifier = Modifier.padding(top = 16.dp),
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatsMetricCard(
                    label = stringResource(R.string.status_time_label),
                    value = formatTime(elapsedSeconds),
                    accent = SudokuPrimary,
                    icon = Icons.Filled.Schedule,
                    modifier = Modifier.weight(1f),
                )
                StatsMetricCard(
                    label = stringResource(R.string.status_mistakes_label),
                    value = mistakes.toString(),
                    accent = if (mistakes > 0) SudokuNeonPink else SudokuNeonGreen,
                    icon = Icons.Filled.Close,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.win_share_subtitle),
                style = DialogTextStyle.copy(
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(SudokuNeonPink, SudokuAccent),
                            ),
                        )
                        .clickable(onClick = onShare)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.win_share),
                        style = DialogTextStyle.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF001A22),
                        ),
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(SudokuNeonGreen, SudokuPrimary),
                            ),
                        )
                        .clickable(onClick = onNewGame)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.game_new),
                        style = DialogTextStyle.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF001A22),
                        ),
                    )
                }
            }
            }
        }
    }
}

fun formatTime(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%d:%02d".format(m, s)
}
