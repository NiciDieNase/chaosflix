package de.nicidienase.chaosflix.leanback.recommendations

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RecommendationBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            setupRecommendationUpdates(context)
        }
    }

    private fun setupRecommendationUpdates(context: Context?) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val intent = Intent(context, ChaosRecommendationsService::class.java)
        val pendingIntent = PendingIntent.getService(context, 0, intent, 0)
        alarmManager?.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, DELAY, AlarmManager.INTERVAL_HALF_HOUR, pendingIntent)
    }

    companion object {
        private const val DELAY = 5_000L
    }
}