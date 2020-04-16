package de.nicidienase.chaosflix.common.viewmodel

import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.nicidienase.chaosflix.common.ChaosflixDatabase
import de.nicidienase.chaosflix.common.ChaosflixPreferenceManager
import de.nicidienase.chaosflix.common.ChaosflixUtil
import de.nicidienase.chaosflix.common.OfflineItemManager
import de.nicidienase.chaosflix.common.mediadata.MediaRepository
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import de.nicidienase.chaosflix.common.userdata.entities.download.OfflineEvent
import de.nicidienase.chaosflix.common.userdata.entities.watchlist.WatchlistItem
import de.nicidienase.chaosflix.common.util.LiveEvent
import de.nicidienase.chaosflix.common.util.SingleLiveEvent
import java.io.File
import java.util.ArrayList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailsViewModel(
    private val database: ChaosflixDatabase,
    private val offlineItemManager: OfflineItemManager,
    private val preferencesManager: ChaosflixPreferenceManager,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private var eventId = MutableLiveData<Long>(0)

    private lateinit var currentEvent: Event

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
        get() = Transformations.switchMap(eventId) {
            mediaRepository.getEvent(it)
        }

    private fun setEventId(eventId: Long?): LiveData<Event?> {
        if (eventId != null) {
            this.eventId.postValue(eventId)
            viewModelScope.launch {
                val findEventByIdSync = database.eventDao().findEventByIdSync(eventId)
                findEventByIdSync?.let {
                    currentEvent = it
                    val recordings = mediaRepository.updateRecordingsForEvent(it)
                    if (waitingForRecordings) {
                        if (recordings != null) {
                            waitingForRecordings = false
                            playEvent()
                        } else {
                            state.postValue(LiveEvent(State.Error, error = "Could not load recordings."))
                        }
                    }
                }
            }
        }
        return event
    }

    suspend fun getEvent(guid: String): Event? {
        return mediaRepository.findEventForGuid(guid)
    }

    fun setEventFromLink(link: String): LiveData<Event?> {
        viewModelScope.launch(Dispatchers.IO) {
            setEventId(mediaRepository.findEventForUri(Uri.parse(link))?.id)
        }
        return event
    }

    fun setEventByGuid(guid: String): LiveData<Event?> {
        viewModelScope.launch(Dispatchers.IO) {
            setEventId(mediaRepository.findEventForGuid(guid)?.id)
        }
        return event
    }

    fun getRecordingForEvent(): LiveData<List<Recording>> {
        return Transformations.switchMap(eventId) {
            mediaRepository.findRecordingsForEvent(it)
        }
    }

    fun getBookmarkForEvent(): LiveData<WatchlistItem?> {
        return Transformations.switchMap(event) {
            if (it != null) {
                database.watchlistItemDao().getItemForEvent(it.guid)
            } else {
                MutableLiveData()
            }
        }
    }

    fun getBookmarkForEvent(guid: String): LiveData<WatchlistItem?> {
        return database.watchlistItemDao().getItemForEvent(guid)
    }

    fun createBookmark() = viewModelScope.launch(Dispatchers.IO) {
        database.eventDao().findEventByIdSync(eventId.value!!)?.guid?.let {
            database.watchlistItemDao().saveItem(WatchlistItem(eventGuid = it))
        } ?: state.postValue(LiveEvent(State.Error, error = "Could not create bookmark."))
    }

    fun removeBookmark() = viewModelScope.launch(Dispatchers.IO) {
        database.eventDao().findEventByIdSync(eventId.value!!)?.guid?.let { guid ->
            database.watchlistItemDao().deleteItem(guid)
        }
    }

    fun getRelatedEvents(): LiveData<List<Event>> = Transformations.switchMap(eventId) {
        if (it != null) {
            mediaRepository.getReleatedEvents(it)
        } else {
            MutableLiveData()
        }
    }

    fun relatedEventSelected(event: Event) {
        val bundle = Bundle()
        bundle.putParcelable(EVENT, event)
        state.postValue(LiveEvent(State.DisplayEvent, data = bundle))
    }

    fun playEvent(autoselect: Boolean = autoselectRecording) = viewModelScope.launch(Dispatchers.IO) {
            val offlineItem = getOfflineItem()
            when {
                offlineItem != null -> playOfflineItem(currentEvent, offlineItem)
                autoselect -> autoSelectRecording()
                else -> letUserSelectRecording()
            }
    }

    private suspend fun autoSelectRecording() = withContext(Dispatchers.IO) {
        val e = mediaRepository.getEventSync(eventId.value!!)
        e?.let { event ->
            val recordings = database.recordingDao().findRecordingByEventSync(event.id)
            if (recordings.isNotEmpty()) {
                val optimalRecording = ChaosflixUtil.getOptimalRecording(recordings, event.originalLanguage)
                val recordingUrl = ChaosflixUtil.getRecordingForThumbs(recordings)?.recordingUrl
                playRecording(event, optimalRecording, recordingUrl)
            } else {
                waitingForRecordings = true
            }
        }
    }

    private suspend fun letUserSelectRecording() = withContext(Dispatchers.IO) {
        // select quality then playEvent
        val recordingList = database.recordingDao().findRecordingByEventSync(eventId.value!!)
                .partition { it.mimeType.startsWith("audio") || it.mimeType.startsWith("video") }
        val items: List<Recording> = recordingList.first
        // TODO: handle subtitles, mimetype application/x-subrip
        if (items.isNotEmpty()) {
            val bundle = Bundle()
            bundle.putParcelable(EVENT, currentEvent)
            bundle.putParcelableArrayList(KEY_SELECT_RECORDINGS, ArrayList(items))
            state.postValue(LiveEvent(State.SelectRecording, data = bundle))
        } else {
            waitingForRecordings = true
            state.postValue(LiveEvent(State.LoadingRecordings))
        }
    }

    private suspend fun getOfflineItem(): Pair<Recording, OfflineEvent>? = withContext(Dispatchers.IO) {
        val event = database.eventDao().findEventByIdSync(eventId.value!!)
                ?: error("No Event found")
        val offlineEvent = database.offlineEventDao().getByEventGuidSuspend(event.guid)
        return@withContext if (offlineEvent != null && fileExists(event.guid)) {
            val recording = database.recordingDao().findRecordingByIdSync(offlineEvent.recordingId)
            recording to offlineEvent
        } else {
            null
        }
    }

    private suspend fun playOfflineItem(event: Event, offlineItem: Pair<Recording, OfflineEvent>) {
        val bundle = Bundle()
        bundle.putString(KEY_LOCAL_PATH, offlineItem.second.localPath)
        bundle.putParcelable(RECORDING, offlineItem.first)
        bundle.putParcelable(EVENT, event)
        if (preferencesManager.externalPlayer) {
            state.postValue(LiveEvent(State.PlayExternal, bundle))
        } else {
            state.postValue(LiveEvent(State.PlayOfflineItem, data = bundle))
        }
    }

    /*
        Post state to play a recoding
     */
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

    fun recordingSelected(e: Event, r: Recording) {
        viewModelScope.launch(Dispatchers.IO) {
            val recordings: List<Recording> = database.recordingDao().findRecordingByEventSync(e.id)
            val url = ChaosflixUtil.getRecordingForThumbs(recordings)?.recordingUrl
            playRecording(e, r, url)
        }
    }

    fun playInExternalPlayer() = viewModelScope.launch {
        database.eventDao().findEventByIdSync(eventId.value!!)?.let {
            postStateWithEventAndRecordings(State.PlayExternal, it)
        }
    }

    fun downloadRecordingForEvent() = viewModelScope.launch {
        database.eventDao().findEventByIdSync(eventId.value!!)?.let {
            downloadRecordingForEvent(it)
        }
    }

    fun downloadRecordingForEvent(event: Event) =
            postStateWithEventAndRecordings(State.DownloadRecording, event)

    fun deleteOfflineItem(): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch(Dispatchers.IO) {
            database.eventDao().findEventByIdSync(eventId.value!!)?.guid?.let { guid ->
                database.offlineEventDao().getByEventGuidSuspend(guid)?.let {
                    offlineItemManager.deleteOfflineItem(it)
                }
            }
            result.postValue(true)
        }
        return result
    }

    fun download(event: Event, recording: Recording) =
            offlineItemManager.download(event, recording)

    private suspend fun fileExists(guid: String): Boolean {
        val offlineItem = database.offlineEventDao().getByEventGuidSuspend(guid)
        return offlineItem != null && File(offlineItem.localPath).exists()
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

    private fun postStateWithEventAndRecordings(s: State, e: Event) {
        viewModelScope.launch(Dispatchers.IO) {
            val items = database.recordingDao().findRecordingByEventSync(e.id)
            val bundle = Bundle()
            bundle.putParcelable(EVENT, e)
            bundle.putParcelableArrayList(KEY_SELECT_RECORDINGS, ArrayList(items))
            state.postValue(LiveEvent(s, bundle))
        }
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
