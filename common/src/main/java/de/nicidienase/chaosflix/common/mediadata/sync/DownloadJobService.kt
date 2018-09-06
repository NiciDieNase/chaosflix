package de.nicidienase.chaosflix.common.mediadata.sync

import android.content.Intent
import android.support.v4.app.JobIntentService
import de.nicidienase.chaosflix.common.DatabaseFactory
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentConference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentItem
import de.nicidienase.chaosflix.common.mediadata.network.ApiFactory

class DownloadJobService : JobIntentService() {

	override fun onHandleWork(intent: Intent) {
		val downloader = Downloader(
				ApiFactory(resources).recordingApi,
				DatabaseFactory(applicationContext).mediaDatabase)
		val entityType: String? = intent.getStringExtra(ENTITY_KEY)

		if (entityType != null) {
			when (entityType) {
				ENTITY_KEY_CONFERENCES -> downloader.updateConferencesAndGroups()
				ENTITY_KEY_EVENTS -> {
					val item = intent.getParcelableExtra<PersistentConference>(ITEM_KEY)
					downloader.updateEventsForConference(item as PersistentConference)
				}
				ENTITY_KEY_RECORDINGS -> {
					val item = intent.getParcelableExtra<PersistentEvent>(ITEM_KEY)
					downloader.updateRecordingsForEvent(item as PersistentEvent)
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