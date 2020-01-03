package de.nicidienase.chaosflix.common.viewmodel

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.nicidienase.chaosflix.common.ChaosflixDatabase
import de.nicidienase.chaosflix.common.OfflineItemManager
import de.nicidienase.chaosflix.common.PreferencesManager
import de.nicidienase.chaosflix.common.mediadata.MediaRepository
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import de.nicidienase.chaosflix.common.userdata.entities.watchlist.WatchlistItem
import de.nicidienase.chaosflix.common.util.LiveEvent
import de.nicidienase.chaosflix.common.util.SingleLiveEvent
import de.nicidienase.chaosflix.common.util.ThreadHandler
import java.io.File

class DetailsViewModel(
    private val database: ChaosflixDatabase,
    private val offlineItemManager: OfflineItemManager,
    private val preferencesManager: PreferencesManager,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    val state: SingleLiveEvent<LiveEvent<State, Bundle, String>> =
            SingleLiveEvent()

    val autoselectRecording: Boolean
        get() = preferencesManager.getAutoselectRecording()

    private val handler = ThreadHandler()

    fun setEvent(event: Event): LiveData<Event?> {
        mediaRepository.updateRecordingsForEvent(event)
        return database.eventDao().findEventByGuid(event.guid)
    }

    fun getRecordingForEvent(event: Event): LiveData<List<Recording>> {
        mediaRepository.updateRecordingsForEvent(event)
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

    fun download(event: Event, recording: Recording) =
            offlineItemManager.download(event, recording)

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

    fun getRelatedEvents(event: Event): LiveData<List<Event>> {
        val data = MutableLiveData<List<Event>>()
        handler.runOnBackgroundThread {
            val guids = database.relatedEventDao().getRelatedEventsForEventSync(event.id).map { it.relatedEventGuid }
            data.postValue(database.eventDao().findEventsByGUIDsSync(guids))
        }
        return data
    }

    fun relatedEventSelected(event: Event) {
        val bundle = Bundle()
        bundle.putParcelable(EVENT, event)
        state.postValue(LiveEvent(State.DisplayEvent, data = bundle))
    }

    fun playEvent(event: Event) {
        handler.runOnBackgroundThread {

            val offlineEvent = database.offlineEventDao().getByEventGuidSync(event.guid)
            if (offlineEvent != null) {
                // Play offlineEvent
                val recording = database.recordingDao().findRecordingByIdSync(offlineEvent.recordingId)
                if (!fileExists(event.guid)) {
                    state.postValue(LiveEvent(State.Error, error = "File is gone"))
                    return@runOnBackgroundThread
                }
                val bundle = Bundle()
                bundle.putString(KEY_LOCAL_PATH, offlineEvent.localPath)
                bundle.putParcelable(RECORDING, recording)
                bundle.putParcelable(EVENT, event)
                if (preferencesManager.externalPlayer) {
                    state.postValue(LiveEvent(State.PlayExternal, bundle))
                } else {
                    state.postValue(LiveEvent(State.PlayOfflineItem, data = bundle))
                }
            } else {
                // select quality then playEvent
                val items = database.recordingDao().findRecordingByEventSync(event.id).toTypedArray()
                val bundle = Bundle()
                bundle.putParcelable(EVENT, event)
                bundle.putParcelableArray(KEY_SELECT_RECORDINGS, items)
                state.postValue(LiveEvent(State.SelectRecording, data = bundle))
            }
        }
    }

    fun playRecording(event: Event, recording: Recording) {
        val bundle = Bundle()
        bundle.putParcelable(RECORDING, recording)
        bundle.putParcelable(EVENT, event)
        if (preferencesManager.externalPlayer) {
            state.postValue(LiveEvent(State.PlayExternal, bundle))
        } else {
            state.postValue(LiveEvent(State.PlayOnlineItem, bundle))
        }
    }

    fun downloadRecordingForEvent(event: Event) =
            postStateWithEventAndRecordings(State.DownloadRecording, event)

    fun playInExternalPlayer(event: Event) = postStateWithEventAndRecordings(State.PlayExternal, event)

    private fun postStateWithEventAndRecordings(s: State, e: Event) {
        handler.runOnBackgroundThread {
            val items = database.recordingDao().findRecordingByEventSync(e.id).toTypedArray()
            val bundle = Bundle()
            bundle.putParcelable(EVENT, e)
            bundle.putParcelableArray(KEY_SELECT_RECORDINGS, items)
            state.postValue(LiveEvent(s, bundle))
        }
    }

    enum class State {
        PlayOfflineItem,
        PlayOnlineItem,
        SelectRecording,
        DownloadRecording,
        DisplayEvent,
        PlayExternal,
        Error
    }

    companion object {
        val TAG = DetailsViewModel::class.simpleName
        const val KEY_LOCAL_PATH = "local_path"
        const val KEY_SELECT_RECORDINGS = "select_recordings"
        const val RECORDING = "recording"
        const val EVENT = "event"
    }
}
