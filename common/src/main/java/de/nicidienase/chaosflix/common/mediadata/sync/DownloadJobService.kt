package de.nicidienase.chaosflix.common.mediadata.sync

import android.content.Intent
import android.support.v4.app.JobIntentService
import de.nicidienase.chaosflix.common.ChaosflixDatabase
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.network.ApiFactory

class DownloadJobService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        val downloader = Downloader(
                ApiFactory.getInstance(resources).recordingApi,
            ChaosflixDatabase.getInstance(applicationContext))
        val entityType: String? = intent.getStringExtra(ENTITY_KEY)

        if (entityType != null) {
            when (entityType) {
                ENTITY_KEY_CONFERENCES -> downloader.updateConferencesAndGroups()
                ENTITY_KEY_EVENTS -> {
                    val item = intent.getParcelableExtra<Conference>(ITEM_KEY)
                    downloader.updateEventsForConference(item as Conference)
                }
                ENTITY_KEY_RECORDINGS -> {
                    val item = intent.getParcelableExtra<Event>(ITEM_KEY)
                    downloader.updateRecordingsForEvent(item as Event)
                }
            }
        }
    }

    companion object {
        val ENTITY_KEY: String = "entity_key"
        val ITEM_KEY: String = "item_key"
        val ENTITY_KEY_CONFERENCES: String = "conferences"
        val ENTITY_KEY_EVENTS: String = "events"
        val ENTITY_KEY_RECORDINGS: String = "recodings"
    }
}