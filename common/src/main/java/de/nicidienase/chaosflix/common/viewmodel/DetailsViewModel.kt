package de.nicidienase.chaosflix.common.viewmodel

import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import de.nicidienase.chaosflix.common.ChaosflixDatabase
import de.nicidienase.chaosflix.common.ChaosflixUtil
import de.nicidienase.chaosflix.common.OfflineItemManager
import de.nicidienase.chaosflix.common.PreferencesManager
import de.nicidienase.chaosflix.common.mediadata.MediaRepository
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import de.nicidienase.chaosflix.common.userdata.entities.watchlist.WatchlistItem
import de.nicidienase.chaosflix.common.util.LiveEvent
import de.nicidienase.chaosflix.common.util.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.ArrayList

class DetailsViewModel(
        private val database: ChaosflixDatabase,
        private val offlineItemManager: OfflineItemManager,
        private val preferencesManager: PreferencesManager,
        private val mediaRepository: MediaRepository
) : ViewModel() {

    private var eventId: Long = 0

    val state: SingleLiveEvent<LiveEvent<State, Bundle, String>> =
            SingleLiveEvent()

    private var waitingForRecordings = false

    var autoselectRecording: Boolean
        get() = preferencesManager.autoselectRecording
        set(value) {
            preferencesManager.autoselectRecording = value
        }

    var autoselectStream: Boolean
        get() = preferencesManager.autoselectStream
        set(value) {
            preferencesManager.autoselectStream = value
        }

    val event: LiveData<Event?>
        get() {
            if (eventId == 0L) {
                error("event not set")
            }
            return database.eventDao().findEventById(eventId)
        }

    fun setEventId(eventId: Long): LiveData<Event?> {
        this.eventId = eventId
        viewModelScope.launch {
            database.eventDao().findEventByIdSync(eventId)?.let {
                val recordings = mediaRepository.updateRecordingsForEvent(it)
                if (waitingForRecordings) {
                    if (recordings != null) {
                        waitingForRecordings = false
                        val bundle = Bundle()
                        bundle.putParcelable(EVENT, it)
                        bundle.putParcelableArrayList(KEY_SELECT_RECORDINGS, ArrayList(recordings))
                        state.postValue(LiveEvent(State.SelectRecording, data = bundle))
                    } else {
                        state.postValue(LiveEvent(State.Error, error = "Could not load recordings."))
                    }
                }
            }
        }
        return mediaRepository.getEvent(eventId)
    }

    private fun loadEvent(eventProvider: suspend ()->Event?): LiveData<Event?> = liveData{
        val event = eventProvider.invoke()
        if(event != null){
            emit(event)
            emitSource(setEventId(event.id))
        } else {
            throw IllegalArgumentException("Event not found")
        }
    }

    fun setEventFromLink(link: String): LiveData<Event?> = loadEvent {
        mediaRepository.findEventForUri(Uri.parse(link))
    }

    fun setEventByGuid(guid: String): LiveData<Event?> = loadEvent {
        mediaRepository.findEventForGuid(guid)
    }

    fun getRecordingForEvent(): LiveData<List<Recording>> {
        return mediaRepository.findRecordingsForEvent(eventId)
    }

    fun getBookmarkForEvent(): LiveData<WatchlistItem?> = liveData {
        mediaRepository.getEventSync(eventId)?.let {
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
        viewModelScope.launch(Dispatchers.IO) {
            database.eventDao().findEventByIdSync(eventId)?.guid?.let { guid ->
                database.offlineEventDao().getByEventGuidSuspend(guid)?.let {
                    offlineItemManager.deleteOfflineItem(it)
                }
            }
            result.postValue(true)
        }
        return result
    }

    fun getRelatedEvents(): LiveData<List<Event>> = mediaRepository.getReleatedEvents(eventId)

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
                val recordingList = database.recordingDao().findRecordingByEventSync(event.id)
                        .partition { it.mimeType.startsWith("audio") || it.mimeType.startsWith("video") }
                val items: List<Recording> = recordingList.first
                // TODO: handle subtitles, mimetype application/x-subrip
                if (items.isNotEmpty()) {
                    val bundle = Bundle()
                    bundle.putParcelable(EVENT, event)
                    bundle.putParcelableArrayList(KEY_SELECT_RECORDINGS, ArrayList(items))
                    state.postValue(LiveEvent(State.SelectRecording, data = bundle))
                } else {
                    state.postValue(LiveEvent(State.LoadingRecordings))
                    waitingForRecordings = true
                }
            }
        }
    }

    fun playRecording(event: Event, recording: Recording, urlForThumbs: String? = null) = viewModelScope.launch(Dispatchers.IO) {
        val progress = database.playbackProgressDao().getProgressForEventSync(event.guid)
        val bundle = Bundle().apply {
            putParcelable(RECORDING, recording)
            putParcelable(EVENT, event)
            putString(THUMBS_URL, urlForThumbs)
            progress?.let {
                putLong(PROGRESS, it.progress)
            }
        }
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
            val items = database.recordingDao().findRecordingByEventSync(e.id)
            val bundle = Bundle()
            bundle.putParcelable(EVENT, e)
            bundle.putParcelableArrayList(KEY_SELECT_RECORDINGS, ArrayList(items))
            state.postValue(LiveEvent(s, bundle))
        }
    }

    fun play(autoselect: Boolean = autoselectRecording) = viewModelScope.launch(Dispatchers.IO) {
        if (autoselect) {
            mediaRepository.getEventSync(eventId)?.let {event ->
                val recordings = database.recordingDao().findRecordingByEventSync(eventId)
                val optimalRecording = ChaosflixUtil.getOptimalRecording(recordings, event.originalLanguage)
                val recordingUrl = ChaosflixUtil.getRecordingForThumbs(recordings)?.recordingUrl
                playRecording(event, optimalRecording, recordingUrl)
            }
        } else {
            playEvent()
        }
    }

    fun recordingSelected(e: Event, r: Recording) {
        viewModelScope.launch(Dispatchers.IO) {
            val recordings: List<Recording> = database.recordingDao().findRecordingByEventSync(e.id)
            val url = ChaosflixUtil.getRecordingForThumbs(recordings)?.recordingUrl
            playRecording(e, r, url)
        }
    }

    enum class State {
        PlayOfflineItem,
        PlayOnlineItem,
        SelectRecording,
        DownloadRecording,
        DisplayEvent,
        PlayExternal,
        Error,
        LoadingRecordings
    }

    companion object {
        val TAG = DetailsViewModel::class.simpleName
        const val KEY_LOCAL_PATH = "local_path"
        const val KEY_SELECT_RECORDINGS = "select_recordings"
        const val RECORDING = "recording"
        const val EVENT = "event"
        const val THUMBS_URL = "thumbs_url"
        const val PROGRESS = "progress"
    }
}
