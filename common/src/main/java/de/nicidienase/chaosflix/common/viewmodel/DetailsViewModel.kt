package de.nicidienase.chaosflix.common.viewmodel

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import de.nicidienase.chaosflix.common.ChaosflixDatabase
import de.nicidienase.chaosflix.common.OfflineItemManager
import de.nicidienase.chaosflix.common.PreferencesManager
import de.nicidienase.chaosflix.common.mediadata.MediaRepository
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import de.nicidienase.chaosflix.common.userdata.entities.watchlist.WatchlistItem
import de.nicidienase.chaosflix.common.util.LiveEvent
import de.nicidienase.chaosflix.common.util.SingleLiveEvent
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val database: ChaosflixDatabase,
    private val offlineItemManager: OfflineItemManager,
    private val preferencesManager: PreferencesManager,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private var eventId: Long = 0
        set(value) {
            field = value
            viewModelScope.launch {
                database.eventDao().findEventByIdSync(value)?.let {
                    mediaRepository.updateRecordingsForEvent(it)
                }
            }
        }

    val state: SingleLiveEvent<LiveEvent<State, Bundle, String>> =
            SingleLiveEvent()

    val autoselectRecording: Boolean
        get() = preferencesManager.getAutoselectRecording()

    val event: LiveData<Event?>
        get() {
            if(eventId == 0L){
                error("event not set")
            }
            return database.eventDao().findEventById(eventId)
        }

    suspend fun setEventFromLink(link: String): LiveData<Event?> {
        val event = database.eventDao().findEventForFrontendUrl(link)
        eventId = event?.id ?: eventId
        return database.eventDao().findEventById(eventId)
    }

    suspend fun setEvent(guid: String): LiveData<Event?> {
        database.eventDao().findEventByGuidSync(guid)?.let {
            eventId = it.id
        }
        return database.eventDao().findEventById(eventId)
    }

    fun getRecordingForEvent(): LiveData<List<Recording>> {
        return database.recordingDao().findRecordingByEvent(eventId)
    }

    fun getBookmarkForEvent(): LiveData<WatchlistItem?> = liveData {
        database.eventDao().findEventByIdSync(eventId)?.let {
            emitSource(
                    database.watchlistItemDao().getItemForEvent(it.guid)
            )
        }
    }

    fun createBookmark() = viewModelScope.launch(Dispatchers.IO) {
        database.eventDao().findEventByIdSync(eventId)?.guid?.let {
            database.watchlistItemDao().saveItem(WatchlistItem(eventGuid = it))
        } ?: state.postValue(LiveEvent(State.Error, error = "Could not create bookmark."))
    }

    fun removeBookmark() = viewModelScope.launch(Dispatchers.IO) {
        database.eventDao().findEventByIdSync(eventId)?.guid?.let { guid ->
            database.watchlistItemDao().deleteItem(guid)
        }
    }

    fun download(event: Event, recording: Recording) =
            offlineItemManager.download(event, recording)

    private suspend fun fileExists(guid: String): Boolean {
        val offlineItem = database.offlineEventDao().getByEventGuidSuspend(guid)
        return offlineItem != null && File(offlineItem.localPath).exists()
    }

    fun deleteOfflineItem(): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch {
            database.eventDao().findEventByIdSync(eventId)?.guid?.let {guid ->
                database.offlineEventDao().getByEventGuidSuspend(guid)?.let {
                    offlineItemManager.deleteOfflineItem(it)
                }
            }
            result.postValue(true)
        }
        return result
    }

    fun getRelatedEvents(event: Event): LiveData<List<Event>> =
            mediaRepository.getReleatedEvents(event, viewModelScope)

    fun relatedEventSelected(event: Event) {
        val bundle = Bundle()
        bundle.putParcelable(EVENT, event)
        state.postValue(LiveEvent(State.DisplayEvent, data = bundle))
    }

    fun playEvent() {
        viewModelScope.launch(Dispatchers.IO) {
            val event = database.eventDao().findEventByIdSync(eventId) ?: error("No Event found")
            val offlineEvent = database.offlineEventDao().getByEventGuidSuspend(event.guid)
            if (offlineEvent != null) {
                // Play offlineEvent
                val recording = database.recordingDao().findRecordingByIdSync(offlineEvent.recordingId)
                if (!fileExists(event.guid)) {
                    state.postValue(LiveEvent(State.Error, error = "File is gone"))
                } else {
                    val bundle = Bundle()
                    bundle.putString(KEY_LOCAL_PATH, offlineEvent.localPath)
                    bundle.putParcelable(RECORDING, recording)
                    bundle.putParcelable(EVENT, event)
                    if (preferencesManager.externalPlayer) {
                        state.postValue(LiveEvent(State.PlayExternal, bundle))
                    } else {
                        state.postValue(LiveEvent(State.PlayOfflineItem, data = bundle))
                    }
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

    fun downloadRecordingForEvent() = viewModelScope.launch {
        database.eventDao().findEventByIdSync(eventId)?.let {
            downloadRecordingForEvent(it)
        }
    }

    fun downloadRecordingForEvent(event: Event) =
            postStateWithEventAndRecordings(State.DownloadRecording, event)

    fun playInExternalPlayer() = viewModelScope.launch {
        database.eventDao().findEventByIdSync(eventId)?.let {
            playInExternalPlayer(it)
        }
    }

    fun playInExternalPlayer(event: Event) = postStateWithEventAndRecordings(State.PlayExternal, event)

    private fun postStateWithEventAndRecordings(s: State, e: Event) {
        viewModelScope.launch(Dispatchers.IO) {
            val items = database.recordingDao().findRecordingByEventSync(e.id).toTypedArray()
            val bundle = Bundle()
            bundle.putParcelable(EVENT, e)
            bundle.putParcelableArray(KEY_SELECT_RECORDINGS, items)
            state.postValue(LiveEvent(s, bundle))
        }
    }

    suspend fun getEvent(eventGuid: String): Event? = database.eventDao().findEventByGuidSync(eventGuid)

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
