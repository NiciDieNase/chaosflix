package de.nicidienase.chaosflix.common.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.Bundle
import de.nicidienase.chaosflix.common.ChaosflixDatabase
import de.nicidienase.chaosflix.common.OfflineItemManager
import de.nicidienase.chaosflix.common.PreferencesManager
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import de.nicidienase.chaosflix.common.mediadata.sync.Downloader
import de.nicidienase.chaosflix.common.userdata.entities.watchlist.WatchlistItem
import de.nicidienase.chaosflix.common.util.LiveEvent
import de.nicidienase.chaosflix.common.util.SingleLiveEvent
import de.nicidienase.chaosflix.common.util.ThreadHandler
import java.io.File

class DetailsViewModel(
		val database: ChaosflixDatabase,
		val offlineItemManager: OfflineItemManager,
		val preferencesManager: PreferencesManager,
		val downloader: Downloader
) : ViewModel() {

	val state: SingleLiveEvent<LiveEvent<State,Bundle,String>>
			= SingleLiveEvent()

	var writeExternalStorageAllowed: Boolean = false

	private val handler = ThreadHandler()

	fun setEvent(event: Event): LiveData<Event?> {
		downloader.updateRecordingsForEvent(event)
		return database.eventDao().findEventByGuid(event.guid)
	}

	fun getRecordingForEvent(event: Event): LiveData<List<Recording>> {
		downloader.updateRecordingsForEvent(event)
		return database.recordingDao().findRecordingByEvent(event.id)
	}

	fun getBookmarkForEvent(guid: String): LiveData<WatchlistItem?> =
			database.watchlistItemDao().getItemForEvent(guid)

	fun createBookmark(guid: String) {
		handler.runOnBackgroundThread {
			database.watchlistItemDao().saveItem(WatchlistItem(eventGuid = guid))
		}
	}

	fun removeBookmark(guid: String) {
		handler.runOnBackgroundThread {
			database.watchlistItemDao().deleteItem(guid)
		}
	}

	fun download(event: Event, recording: Recording)
			= offlineItemManager.download(event, recording)

	private fun fileExists(guid: String): Boolean {
		val offlineItem = database.offlineEventDao().getByEventGuidSync(guid)
		return offlineItem != null && File(offlineItem.localPath).exists()
	}

	fun deleteOfflineItem(event: Event): LiveData<Boolean> {
		val result = MutableLiveData<Boolean>()
		handler.runOnBackgroundThread {
			database.offlineEventDao().getByEventGuidSync(event.guid)?.let {
				offlineItemManager.deleteOfflineItem(it)
			}
			result.postValue(true)
		}
		return result
	}

	fun getRelatedEvents(event: Event): LiveData<List<Event>>{
		val data = MutableLiveData<List<Event>>()
		handler.runOnBackgroundThread {
			val guids = database.relatedEventDao().getRelatedEventsForEventSync(event.id).map { it.relatedEventGuid }
			data.postValue(database.eventDao().findEventsByGUIDsSync(guids))
		}
		return data
	}

	fun playEvent(event: Event) {
		handler.runOnBackgroundThread {

			val offlineEvent = database.offlineEventDao().getByEventGuidSync(event.guid)
			if(offlineEvent != null){
				// Play offlineEvent
				val recording = database.recordingDao().findRecordingByIdSync(offlineEvent.recordingId)
				if(!fileExists(event.guid)){
					state.postValue(LiveEvent(State.Error, error = "File is gone"))
					return@runOnBackgroundThread
				}
				val bundle = Bundle()
				bundle.putString(KEY_LOCAL_PATH, offlineEvent.localPath)
				bundle.putParcelable(KEY_PLAY_RECORDING, recording)
				state.postValue(LiveEvent(State.PlayOfflineItem, data = bundle))
			} else {
				// select quality then playEvent
				val items = database.recordingDao().findRecordingByEventSync(event.id).toTypedArray()
				val bundle = Bundle()
				bundle.putParcelableArray(KEY_SELECT_RECORDINGS, items)
				state.postValue(LiveEvent(State.SelectRecording, data = bundle ))
			}
		}
	}

	fun playRecording(recording: Recording){
		val bundle = Bundle()
		bundle.putParcelable(KEY_PLAY_RECORDING, recording)
		state.postValue(LiveEvent(State.PlayOnlineItem, data = bundle))
	}

	fun offlineItemExists(event: Event): LiveData<Boolean> {
		val liveData = MutableLiveData<Boolean>()
		handler.runOnBackgroundThread {
			database.offlineEventDao().getByEventGuidSync(event.guid)
		}
		return liveData
	}

	fun getAutoselectStream() = preferencesManager.getAutoselectStream()

	fun getAutoselectRecording() = preferencesManager.getAutoselectRecording()


	enum class State{
		PlayOfflineItem, PlayOnlineItem, SelectRecording, Error
	}

	companion object {
		val TAG = DetailsViewModel::class.simpleName
		val KEY_LOCAL_PATH = "local_path"
		val KEY_SELECT_RECORDINGS = "select_recordings"
		val KEY_PLAY_RECORDING = "play_recording"
	}
}

