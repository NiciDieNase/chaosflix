package de.nicidienase.chaosflix.leanback.recommendations

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.preference.PreferenceManager
import android.util.Log
import androidx.recommendation.app.ContentRecommendation
import com.bumptech.glide.Glide
import de.nicidienase.chaosflix.common.ChaosflixPreferenceManager
import de.nicidienase.chaosflix.common.mediadata.MediaRepository
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.leanback.R
import de.nicidienase.chaosflix.leanback.detail.DetailsActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException

class ChaosRecommendationsService : IntentService("ChaosRecommendationService") {

    private val ioScope = CoroutineScope(Dispatchers.IO + Job())

    override fun onHandleIntent(intent: Intent?) {
        val preferenceManager = ChaosflixPreferenceManager(PreferenceManager.getDefaultSharedPreferences(applicationContext))
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val mediaRepository = ViewModelFactory.getInstance(this).mediaRepository
        if(!preferenceManager.recommendationsEnabled){
            Log.i(TAG, "recommendations are disabled, returning")
            notificationManager?.cancelAll()
            return
        }
        ioScope.launch {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Log.i(TAG, "updating recommendation channels")
                ChannelManager.setupChannels(this@ChaosRecommendationsService, mediaRepository, preferenceManager)
            } else {
                Log.i(TAG, "updating recommendation notifications")
                notificationManager?.let { setupRecommendationNotifications(mediaRepository, it) }
            }
        }
    }

    private suspend fun setupRecommendationNotifications(mediaRepository: MediaRepository, notificationManager: NotificationManager) {
        val recommendations = mediaRepository.getHomescreenRecommendations()

        val cardWidth: Int = resources.getDimensionPixelSize(R.dimen.recommendation_thumb_width)
        val cardHeight: Int = resources.getDimensionPixelSize(R.dimen.recommendation_thumb_height)
        try {
            val builder = ContentRecommendation.Builder().setBadgeIcon(R.drawable.chaosflix_icon)

            for (event in recommendations) {
                Log.d(TAG, "Recommendation - " + event.title)

                val bitmap: Bitmap = Glide.with(application)
                        .asBitmap()
                        .load(event.thumbUrl)
                        .submit(cardWidth, cardHeight)
                        .get()

                val id = event.id.toInt()
                val contentRecommendation = builder.setIdTag("Event-$id")
                        .setTitle(event.title)
                        .setText(event.subtitle)
                        .setContentImage(bitmap)
                        .setContentIntentData(ContentRecommendation.INTENT_TYPE_ACTIVITY, buildPendingIntent(event), 0, null)
                        .setRunningTime(event.length)
                        .setContentTypes(arrayOf(ContentRecommendation.CONTENT_TYPE_VIDEO))
//                            .setBackgroundImageUri(event.posterUrl) // not supported on fireTV
                        .build()
                val notification = contentRecommendation.getNotificationObject(applicationContext)
                notification.extras.putString("com.amazon.extra.DISPLAY_NAME", "Chaosflix")
                notification.extras.putString("com.amazon.extra.PREVIEW_URL", event.posterUrl)
                notification.extras.putString("com.amazon.extra.LONG_DESCRIPTION", event.description)
                notification.extras.putInt("com.amazon.extra.LIVE_CONTENT", 0)
//                    notification.extras.putInt("com.amazon.extra.CONTENT_CUSTOMER_RATING", 9)
                notification.extras.putInt("com.amazon.extra.CONTENT_CUSTOMER_RATING_COUNT", event.viewCount)
                notification.extras.putIntArray("com.amazon.extra.ACTION_OPTION", intArrayOf(2))

                notificationManager.notify(id, notification)

                Log.d(TAG, "Added notification for ${event.title}")
            }
        } catch (e: IOException) {
            Log.e(TAG, "Unable to update recommendation", e)
        }
        Log.d(TAG, "done generating recommendations")
    }

    private fun buildPendingIntent(event: Event): Intent {
        return Intent(this, DetailsActivity::class.java).apply {
            putExtra(DetailsActivity.EVENT, event)
        }
    }

    companion object {
        private val TAG = ChaosRecommendationsService::class.java.simpleName
    }
}
