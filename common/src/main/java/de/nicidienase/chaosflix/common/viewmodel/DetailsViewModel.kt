package de.nicidienase.chaosflix.common.viewmodel

import android.net.Uri
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
import de.nicidienase.chaosflix.common.util.SingleLiveEvent
import de.nicidienase.chaosflix.touch.browse.cast.CastService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class DetailsViewModel(
        private val database: ChaosflixDatabase,
        private val offlineItemManager: OfflineItemManager,
        private val preferencesManager: ChaosflixPreferenceManager,
        private val mediaRepository: MediaRepository,
        private val castService: CastService
) : ViewModel() {

    private var eventId = MutableLiveData<Long>(0)

    private lateinit var currentEvent: Event

    val state: SingleLiveEvent<State> =
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
                            state.postValue(State.Error("Could not load recordings."))
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
        } ?: state.postValue(State.Error("Could not create bookmark."))
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
        state.postValue(State.DisplayEvent(event))
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
            state.postValue(State.SelectRecording(currentEvent, items))
        } else {
            waitingForRecordings = true
            state.postValue(State.LoadingRecordings)
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

    private fun playOfflineItem(event: Event, offlineItem: Pair<Recording, OfflineEvent>) {
        if (preferencesManager.externalPlayer) {
            state.postValue(State.PlayLocalFileExternal(offlineItem.second.localPath))
        } else {
            state.postValue(State.PlayOfflineItem(event, offlineItem.first, offlineItem.second.localPath))
        }
    }

    /*
        Post state to play a recoding
     */
    fun playRecording(event: Event, recording: Recording, urlForThumbs: String? = null) = viewModelScope.launch(Dispatchers.IO) {
        val progress = database.playbackProgressDao().getProgressForEventSync(event.guid)
        when {
            castService.connected -> {
                withContext(Dispatchers.Main) {
                    castService.loadMediaAndPlay(recording, event, progress)
                }
            }
            preferencesManager.externalPlayer -> {
                state.postValue(State.PlayExternal(event, listOf(recording)))
            }
            else -> {
                state.postValue(State.PlayOnlineItem(event, recording, progress?.progress, urlForThumbs))
            }
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
        val id = eventId.value
        if(id != null){
            val event = mediaRepository.getEventSync(id)
            val recordings = mediaRepository.findRecordingsForEventSync(id!!)
            if(event != null){
                state.postValue(State.PlayExternal(event, recordings))
            }else {
                error("Event should not be null")
            }
        } else {
            error("Id should not be null")
        }
    }

    fun downloadRecordingForEvent() = viewModelScope.launch {
        val id = eventId.value
        if(id != null){
            val event = database.eventDao().findEventByIdSync(id)
            if(event != null){
                val recordings = mediaRepository.findRecordingsForEventSync(id)
                state.postValue(State.DownloadRecording(event, recordings))
            } else {
                error("Event should not be null")
            }
        } else {
            error("Id should not be null")
        }
    }

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

    sealed class State {
        data class PlayOnlineItem(val event: Event, val recording: Recording, val progress: Long?, val urlForThumbs: String?) : State()
        data class PlayOfflineItem(val event: Event, val recording: Recording, val localFile: String) : State()
        data class SelectRecording(val event: Event, val recordings: List<Recording>) : State()
        data class DownloadRecording(val event: Event, val recordings: List<Recording>) : State()
        data class DisplayEvent(val event: Event) : State()
        data class PlayExternal(val event: Event, val recordings: List<Recording>) : State()
        data class PlayLocalFileExternal(val path: String) : State()
        data class Error(val message: String) : State()
        object LoadingRecordings : State()
        data class OpenCustomTab(val uri: Uri) : State()
    }

    fun openLink() {
        eventId.value?.let {
            viewModelScope.launch {
                val eventSync = mediaRepository.getEventSync(it)
                eventSync?.link?.let {
                    openCustomTab(it)
                }
            }
        }
    }

    private fun openCustomTab(link: String?) {
        if (link != null) {
            try {
                state.postValue(State.OpenCustomTab(Uri.parse(link)))
            } catch (e: Exception) {

            }
        }
    }

    companion object {
        val TAG = DetailsViewModel::class.simpleName
    }
}
