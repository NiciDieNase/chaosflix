package de.nicidienase.chaosflix.leanback.recommendations

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.tvprovider.media.tv.TvContractCompat
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory

class RecommendationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                setupRecommendationUpdates(context)
                context?.let { setup(it) }
            }
            TvContractCompat.ACTION_WATCH_NEXT_PROGRAM_BROWSABLE_DISABLED -> if (context != null) handleRemove(intent, context)
            TvContractCompat.ACTION_PREVIEW_PROGRAM_BROWSABLE_DISABLED -> if (context != null) handleRemove(intent, context)
            else -> Log.d(TAG, intent.toString())
        }
    }

    private fun setupRecommendationUpdates(context: Context?) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val intent = Intent(context, ChaosRecommendationsService::class.java)
        val pendingIntent = PendingIntent.getService(context, 0, intent, 0)
        alarmManager?.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, DELAY, AlarmManager.INTERVAL_HALF_HOUR, pendingIntent)
    }

    private fun handleRemove(intent: Intent, context: Context) {
        val id = intent.getLongExtra(TvContractCompat.EXTRA_PREVIEW_PROGRAM_ID, 0)
        Log.d(TAG, "Id: $id")
        val mediaRepository = ViewModelFactory.getInstance(context).mediaRepository
        mediaRepository.resetRecommendationId(id)
    }

    companion object {
        private val TAG = RecommendationBroadcastReceiver::class.java.simpleName

        private const val DELAY = 5_000L

        fun setup(context: Context) {
            val filter = IntentFilter(TvContractCompat.ACTION_WATCH_NEXT_PROGRAM_BROWSABLE_DISABLED).apply {
                addAction(TvContractCompat.ACTION_PREVIEW_PROGRAM_BROWSABLE_DISABLED)
            }
            context.registerReceiver(RecommendationBroadcastReceiver(), filter)
        }
    }
}
