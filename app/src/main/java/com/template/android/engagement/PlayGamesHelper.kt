package com.template.android.engagement

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.google.android.gms.tasks.Task
import com.template.android.R
import com.template.android.analytics.AppAnalytics
import com.template.android.data.AchievementId
import com.template.android.ui.sudoku.SudokuDifficulty

/**
 * Play Games Services v2: inicio de sesión automático al abrir (sin botón obligatorio).
 * Los leaderboards deben abrirse con [startLeaderboardUi] (Activity Result launcher),
 * no con startActivity() directo — Play Games valida el paquete firmante.
 */
object PlayGamesHelper {

    private const val TAG = "PlayGamesHelper"

    fun isEnabled(context: Context): Boolean = isConfigured(context)

    fun initialize(context: Context) {
        if (!isConfigured(context)) return
        PlayGamesSdk.initialize(context)
    }

    fun signInSilently(activity: Activity) {
        if (!isConfigured(activity)) return
        if (!isGooglePlayServicesAvailable(activity)) return
        val signInClient = PlayGames.getGamesSignInClient(activity)
        signInClient.isAuthenticated.addOnCompleteListener { task ->
            val authenticated = task.isSuccessful && task.result.isAuthenticated
            if (authenticated) {
                AppAnalytics.logPlayGamesSignIn("silent_success")
                return@addOnCompleteListener
            }
            AppAnalytics.logPlayGamesSignIn("silent_pending")
        }
    }

    fun hasLeaderboards(context: Context): Boolean =
        isConfigured(context) &&
            SudokuDifficulty.entries.any { leaderboardIdFor(context, it) != null }

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
            achievementResFor(id)?.let { res -> unlockPlayAchievement(activity, res) }
        }
    }

    fun unlockPlayAchievement(activity: Activity, achievementRes: Int) {
        val id = activity.getString(achievementRes).trim()
        if (id.isEmpty()) return
        PlayGames.getAchievementsClient(activity).unlock(id)
    }

    fun showLeaderboard(
        activity: Activity,
        difficulty: SudokuDifficulty,
        startLeaderboardUi: (Intent) -> Unit,
    ) {
        if (!isConfigured(activity)) return
        val id = leaderboardIdFor(activity, difficulty) ?: return
        withSignedIn(activity) {
            launchLeaderboardIntent(
                activity = activity,
                task = PlayGames.getLeaderboardsClient(activity).getLeaderboardIntent(id),
                source = "single_${difficulty.name.lowercase()}",
                startLeaderboardUi = startLeaderboardUi,
            )
        }
    }

    fun showAllLeaderboards(
        activity: Activity,
        preferredDifficulty: SudokuDifficulty? = null,
        startLeaderboardUi: (Intent) -> Unit,
    ) {
        if (!isConfigured(activity)) return
        withSignedIn(activity) {
            openLeaderboards(
                activity = activity,
                preferredDifficulty = preferredDifficulty,
                startLeaderboardUi = startLeaderboardUi,
                allowSignInRetry = true,
            )
        }
    }

    fun showAchievements(activity: Activity) {
        if (!isConfigured(activity)) return
        withSignedIn(activity) {
            PlayGames.getAchievementsClient(activity).achievementsIntent
                .addOnSuccessListener { intent -> activity.startActivity(intent) }
                .addOnFailureListener { error ->
                    logFailure("achievements", error)
                    toast(activity, R.string.play_games_leaderboards_failed)
                }
        }
    }

    private fun openLeaderboards(
        activity: Activity,
        preferredDifficulty: SudokuDifficulty?,
        startLeaderboardUi: (Intent) -> Unit,
        allowSignInRetry: Boolean,
    ) {
        val client = PlayGames.getLeaderboardsClient(activity)
        val preferredId = preferredDifficulty?.let { leaderboardIdFor(activity, it) }

        fun retryOrFallback(error: Exception?, skipPreferred: String?) {
            if (allowSignInRetry && shouldRetrySignIn(error)) {
                withSignedIn(activity, forceInteractive = true) {
                    openLeaderboards(
                        activity = activity,
                        preferredDifficulty = preferredDifficulty,
                        startLeaderboardUi = startLeaderboardUi,
                        allowSignInRetry = false,
                    )
                }
                return
            }
            openAnyLeaderboard(
                activity = activity,
                client = client,
                skipId = skipPreferred,
                startLeaderboardUi = startLeaderboardUi,
                onFailure = { showLeaderboardsError(activity, it) },
            )
        }

        if (preferredId != null) {
            launchLeaderboardIntent(
                activity = activity,
                task = client.getLeaderboardIntent(preferredId),
                source = "preferred_${preferredDifficulty!!.name.lowercase()}",
                startLeaderboardUi = startLeaderboardUi,
                onFailure = { retryOrFallback(it, preferredId) },
            )
            return
        }

        openAnyLeaderboard(
            activity = activity,
            client = client,
            startLeaderboardUi = startLeaderboardUi,
            onFailure = { retryOrFallback(it, null) },
        )
    }

    private fun withSignedIn(
        activity: Activity,
        forceInteractive: Boolean = false,
        onReady: () -> Unit,
    ) {
        if (!isGooglePlayServicesAvailable(activity)) {
            toast(activity, R.string.play_games_unavailable)
            return
        }
        val signInClient = PlayGames.getGamesSignInClient(activity)
        signInClient.isAuthenticated.addOnCompleteListener { authTask ->
            val authenticated = authTask.isSuccessful && authTask.result.isAuthenticated
            if (authenticated && !forceInteractive) {
                onReady()
                return@addOnCompleteListener
            }
            signInClient.signIn().addOnCompleteListener { signInTask ->
                if (!signInTask.isSuccessful) {
                    AppAnalytics.logPlayGamesSignIn("interactive_failed")
                    logFailure("sign_in_task", signInTask.exception)
                    toast(activity, R.string.play_games_sign_in_failed)
                    return@addOnCompleteListener
                }
                signInClient.isAuthenticated.addOnCompleteListener { retryTask ->
                    val signedIn = retryTask.isSuccessful && retryTask.result.isAuthenticated
                    if (signedIn) {
                        AppAnalytics.logPlayGamesSignIn("interactive_success")
                        onReady()
                    } else {
                        AppAnalytics.logPlayGamesSignIn("interactive_failed")
                        logFailure("sign_in", retryTask.exception)
                        toast(activity, R.string.play_games_sign_in_failed)
                    }
                }
            }
        }
    }

    private fun openAnyLeaderboard(
        activity: Activity,
        client: com.google.android.gms.games.LeaderboardsClient,
        skipId: String? = null,
        startLeaderboardUi: (Intent) -> Unit,
        onFailure: (Exception?) -> Unit,
    ) {
        launchLeaderboardIntent(
            activity = activity,
            task = client.allLeaderboardsIntent,
            source = "all",
            startLeaderboardUi = startLeaderboardUi,
            onFailure = { error ->
                logFailure("leaderboard_all", error)
                val fallbackId = SudokuDifficulty.entries
                    .mapNotNull { leaderboardIdFor(activity, it) }
                    .firstOrNull { it != skipId }
                if (fallbackId != null) {
                    launchLeaderboardIntent(
                        activity = activity,
                        task = client.getLeaderboardIntent(fallbackId),
                        source = "fallback",
                        startLeaderboardUi = startLeaderboardUi,
                        onFailure = { showLeaderboardsError(activity, it) },
                    )
                } else {
                    onFailure(error)
                }
            },
        )
    }

    private fun launchLeaderboardIntent(
        activity: Activity,
        task: Task<Intent>,
        source: String,
        startLeaderboardUi: (Intent) -> Unit,
        onFailure: ((Exception?) -> Unit)? = null,
    ) {
        task
            .addOnSuccessListener { intent ->
                AppAnalytics.logPlayGamesLeaderboardOpened(source)
                startLeaderboardUi(intent)
            }
            .addOnFailureListener { error ->
                reportLeaderboardFailure(source, error)
                if (onFailure != null) {
                    onFailure(error)
                } else {
                    showLeaderboardsError(activity, error)
                }
            }
    }

    private fun shouldRetrySignIn(error: Exception?): Boolean {
        val code = (error as? ApiException)?.statusCode ?: return false
        return code == com.google.android.gms.common.api.CommonStatusCodes.SIGN_IN_REQUIRED ||
            code == 26507
    }

    private fun showLeaderboardsError(activity: Activity, error: Exception? = null) {
        reportLeaderboardFailure("final", error)
        toast(activity, R.string.play_games_leaderboards_failed)
    }

    private fun reportLeaderboardFailure(stage: String, error: Exception?) {
        val code = (error as? ApiException)?.statusCode
        AppAnalytics.logPlayGamesLeaderboardFailed(stage, code)
        logFailure("leaderboard_$stage", error)
    }

    private fun logFailure(stage: String, error: Exception?) {
        val code = (error as? ApiException)?.statusCode
        val detail = buildString {
            append(error?.message ?: "unknown")
            if (code != null) append(" (status=$code)")
        }
        Log.w(TAG, "Play Games failed at $stage: $detail")
    }

    private fun toast(activity: Activity, messageRes: Int) {
        Toast.makeText(activity, activity.getString(messageRes), Toast.LENGTH_LONG).show()
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
