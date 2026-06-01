package com.template.android.engagement

import android.app.Activity
import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.template.android.R
import com.template.android.analytics.AppAnalytics

/**
 * Play Games opcional: configura IDs en strings.xml (play_games_*).
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

    fun submitScore(activity: Activity, score: Long, leaderboardKey: String = "default") {
        if (!isConfigured(activity)) return
        val leaderboardId = activity.getString(R.string.play_games_leaderboard_default).trim()
        if (leaderboardId.isEmpty()) return
        PlayGames.getLeaderboardsClient(activity).submitScore(leaderboardId, score)
        AppAnalytics.logPlayGamesScoreSubmit(leaderboardKey)
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

    private fun isConfigured(context: Context): Boolean =
        context.getString(R.string.play_games_project_id).trim().isNotEmpty()

    private fun isGooglePlayServicesAvailable(context: Context): Boolean {
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        return code == ConnectionResult.SUCCESS
    }
}
