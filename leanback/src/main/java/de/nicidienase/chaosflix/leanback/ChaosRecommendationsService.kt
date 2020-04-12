package de.nicidienase.chaosflix.leanback

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
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.leanback.detail.DetailsActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException

class ChaosRecommendationsService : IntentService("ChaosRecommendationService") {

    private val ioScope = CoroutineScope(Dispatchers.IO + Job())

    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "Updating Recommendation")

        val mediaRepository = ViewModelFactory.getInstance(this).mediaRepository
        val preferenceManager = ChaosflixPreferenceManager(PreferenceManager.getDefaultSharedPreferences(applicationContext))
//        if(preferenceManager.recommendationsGenerated){
//            Log.d(TAG, "already generated, returning")
//            Toast.makeText(this, "already generated, returning", Toast.LENGTH_SHORT).show()
//            return
//        }
        ioScope.launch {
            val recommendations = mediaRepository.getRecommendations()
            var count = 0

            val cardWidth: Int = resources.getDimensionPixelSize(R.dimen.conference_card_width)
            val cardHeight: Int = resources.getDimensionPixelSize(R.dimen.conference_card_height)
            try {
//                val builder = RecommendationBuilder()
                val builder = ContentRecommendation.Builder().setBadgeIcon(R.drawable.chaosflix_icon)
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

                for (event in recommendations) {
                    Log.d(TAG, "Recommendation - " + event.title)

                    val bitmap: Bitmap = Glide.with(getApplication())
                            .asBitmap()
                            .load(event.thumbUrl)
                            .submit(cardWidth, cardHeight) // Only use for synchronous .get()
                            .get()

                    val id = event.id.toInt()
                    val contentRecommendation = builder.setIdTag("Event-$id")
                            .setTitle(event.title)
                            .setText(event.subtitle)
                            .setContentImage(bitmap)
                            .setContentIntentData(ContentRecommendation.INTENT_TYPE_ACTIVITY, buildPendingIntent(event), 0, null)
                            .build()
                    val notification = contentRecommendation.getNotificationObject(applicationContext)
                    notificationManager?.notify(id, notification)

                    Log.d(TAG, "Added notification for ${event.title}")

                    if (++ count >= MAX_RECOMMENDATIONS) {
                        break
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Unable to update recommendation", e)
            }
            Log.d(TAG, "done generating recommendations")
            preferenceManager.recommendationsGenerated = false
        }
    }

    private fun buildPendingIntent(event: Event): Intent {
        val detailsIntent = Intent(this, DetailsActivity::class.java).apply {
            putExtra(DetailsActivity.EVENT, event)
        }
        return detailsIntent
//        val builder = TaskStackBuilder.create(this)
//                .addParentStack(ConferencesActivity::class.java)
//                .addNextIntent(detailsIntent)
//        detailsIntent.action = event.guid
//        return builder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    companion object {
        private val TAG = ChaosRecommendationsService::class.java.simpleName
        private const val MAX_RECOMMENDATIONS = 6
    }
}
