package com.template.android.engagement

import android.app.Activity
import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.template.android.R
import com.template.android.analytics.AppAnalytics
import com.template.android.data.AchievementId
import com.template.android.ui.sudoku.SudokuDifficulty

/**
 * Play Games opcional: configura IDs en legal.xml (play_games_*).
 * Si están vacíos, todas las llamadas son no-op.
 */
object PlayGamesHelper {

    fun initialize(context: Context) {
        if (!isConfigured(context)) return
        PlayGamesSdk.initialize(context)
    }

    fun signInSilently(activity: Activity) {
        if (!isConfigured(activity)) return
        if (!isGooglePlayServicesAvailable(activity)) return
        PlayGames.getGamesSignInClient(activity).isAuthenticated.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result.isAuthenticated) {
                AppAnalytics.logPlayGamesSignIn("silent_success")
            }
        }
    }

    fun submitScore(activity: Activity, difficulty: SudokuDifficulty, elapsedSeconds: Int) {
        if (!isConfigured(activity)) return
        val leaderboardId = leaderboardIdFor(activity, difficulty) ?: return
        val score = (10_000L - elapsedSeconds.coerceAtMost(9_999)).coerceAtLeast(1)
        PlayGames.getLeaderboardsClient(activity).submitScore(leaderboardId, score)
        AppAnalytics.logPlayGamesScoreSubmit(difficulty.name.lowercase())
    }

    fun syncAchievements(activity: Activity, unlocks: List<AchievementId>) {
        if (!isConfigured(activity) || unlocks.isEmpty()) return
        unlocks.forEach { id ->
            achievementResFor(id)?.let { res -> unlockAchievement(activity, res) }
        }
    }

    fun unlockAchievement(activity: Activity, achievementRes: Int) {
        val id = activity.getString(achievementRes).trim()
        if (id.isEmpty()) return
        PlayGames.getAchievementsClient(activity).unlock(id)
    }

    fun showLeaderboard(activity: Activity) {
        val id = activity.getString(R.string.play_games_leaderboard_default).trim()
        if (id.isEmpty()) return
        PlayGames.getLeaderboardsClient(activity).getLeaderboardIntent(id)
            .addOnSuccessListener { intent -> activity.startActivity(intent) }
    }

    private fun achievementResFor(id: AchievementId): Int? = when (id) {
        AchievementId.FIRST_WIN -> R.string.play_games_achievement_first_win
        AchievementId.WINS_10 -> R.string.play_games_achievement_wins_10
        AchievementId.WIN_HARD -> R.string.play_games_achievement_win_hard
        AchievementId.STREAK_3 -> R.string.play_games_achievement_streak_3
        AchievementId.PLAY_7_DAYS -> R.string.play_games_achievement_play_7_days
        AchievementId.DAILY_CHALLENGE -> R.string.play_games_achievement_daily
    }

    private fun leaderboardIdFor(context: Context, difficulty: SudokuDifficulty): String? {
        val res = when (difficulty) {
            SudokuDifficulty.EASY -> R.string.play_games_leaderboard_easy
            SudokuDifficulty.MEDIUM -> R.string.play_games_leaderboard_medium
            SudokuDifficulty.HARD -> R.string.play_games_leaderboard_hard
        }
        return context.getString(res).trim().takeIf { it.isNotEmpty() }
            ?: context.getString(R.string.play_games_leaderboard_default).trim().takeIf { it.isNotEmpty() }
    }

    private fun isConfigured(context: Context): Boolean =
        context.getString(R.string.play_games_project_id).trim().isNotEmpty()

    private fun isGooglePlayServicesAvailable(context: Context): Boolean {
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        return code == ConnectionResult.SUCCESS
    }
}
