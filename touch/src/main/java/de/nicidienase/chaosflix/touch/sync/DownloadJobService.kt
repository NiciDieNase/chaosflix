package de.nicidienase.chaosflix.touch.sync

import android.content.Intent
import android.support.v4.app.JobIntentService
import de.nicidienase.chaosflix.touch.ViewModelFactory

class DownloadJobService : JobIntentService() {

	override fun onHandleWork(intent: Intent) {
		val downloader = Downloader(ViewModelFactory.recordingApi, ViewModelFactory.database)
		val entity: String? = intent.getStringExtra(ENTITY_KEY)
		val id: Long = intent.getLongExtra(ID_KEY, -1)
		if (entity != null) {
			when (entity) {
//                ENTITY_KEY_EVERYTHING -> downloader.updateEverything()
				ENTITY_KEY_CONFERENCES -> downloader.updateConferencesAndGroups()
				ENTITY_KEY_EVENTS -> downloader.updateEventsForConference(id)
				ENTITY_KEY_RECORDINGS -> downloader.updateRecordingsForEvent(id)
			}
		}
	}

	companion object {
		val ENTITY_KEY: String = "entity_key"
		//        val ENTITY_KEY_EVERYTHING = "everything"
		val ENTITY_KEY_CONFERENCES: String = "conferences"
		val ENTITY_KEY_EVENTS: String = "events"
		val ENTITY_KEY_RECORDINGS: String = "recodings"
		val ID_KEY: String = "id_key"
	}
}