package de.nicidienase.chaosflix.leanback

import android.app.IntentService
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.app.NotificationCompat
import de.nicidienase.chaosflix.common.ChaosflixPreferenceManager
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.leanback.conferences.ConferencesActivity
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

        if(preferenceManager.recommendationsGenerated){
            Log.d(TAG, "already generated, returning")
            return
        }

        ioScope.launch {
            val recommendations = mediaRepository.getRecommendations()

            var count = 0

            try {
                val builder = RecommendationBuilder()
                        .setContext(applicationContext)
                        .setSmallIcon(R.drawable.chaosflix_icon)
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

                for (event in recommendations) {
                    Log.d(TAG, "Recommendation - " + event.title)

                    val id = event.id.toInt()
                    val notification = builder.setId(id)
                            .setTitle(event.title)
                            .setDescription(event.description ?: "")
                            .setImage(event.thumbUrl)
                            .setPendingIntent(buildPendingIntent(event))
                            .build()
                    notificationManager?.notify(id, notification)
                    Log.d(TAG, "Added notification for ${event.title}")

                    if(++ count >= MAX_RECOMMENDATIONS){
                        break
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Unable to update recommendation", e)
            }
            preferenceManager.recommendationsGenerated = true
        }
    }

    private fun buildPendingIntent(event: Event): PendingIntent {
        val detailsIntent = Intent(this, DetailsActivity::class.java).apply {
            putExtra(DetailsActivity.EVENT, event)
        }
        val builder = TaskStackBuilder.create(this)
                .addParentStack(ConferencesActivity::class.java)
                .addNextIntent(detailsIntent)

        detailsIntent.setAction(event.guid)

        return builder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    companion object {
        private val TAG = ChaosRecommendationsService::class.java.simpleName

        private const val MAX_RECOMMENDATIONS = 5
    }

    class RecommendationBuilder {

        private var id: Int = 0
        private var icon: Int = 0
        private lateinit var intent: PendingIntent
        private lateinit var context: Context
        private lateinit var imageUri: String
        private lateinit var backgroundUri: String
        private lateinit var description: String
        private lateinit var title: String

        fun setTitle(title: String): RecommendationBuilder {
            this.title = title
            return this
        }

        fun setDescription(description: String): RecommendationBuilder {
            this.description = description
            return this
        }

        fun setImage(uri: String): RecommendationBuilder {
            imageUri = uri
            return this
        }

        fun setBackground(uri: String): RecommendationBuilder {
            backgroundUri = uri
            return this
        }


        fun setContext(context: Context): RecommendationBuilder {
            this.context = context
            return this
        }

        fun setPendingIntent(intent: PendingIntent): RecommendationBuilder {
            this.intent = intent
            return this
        }

        @Throws(IOException::class)
        fun build(): Notification {
            return NotificationCompat.BigPictureStyle(
                    NotificationCompat.Builder(context)
                            .setContentTitle(title)
                            .setContentText(description)
                            .setPriority(id)
                            .setLocalOnly(true)
                            .setOngoing(true)
                            .setColor(context.resources.getColor(R.color.brand_dark))
                            .setCategory(Notification.CATEGORY_RECOMMENDATION)
                            .setSmallIcon(icon)
                            .setContentIntent(intent)
            ).build()
        }

        fun setSmallIcon(icon: Int): RecommendationBuilder {
            this.icon = icon
            return this
        }

        fun setId(id: Int): RecommendationBuilder {
            this.id = id
            return this
        }

    }
}