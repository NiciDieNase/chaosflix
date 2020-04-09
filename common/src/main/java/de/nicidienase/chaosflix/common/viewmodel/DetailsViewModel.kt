package de.nicidienase.chaosflix.common.viewmodel

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
import java.io.File
import java.util.ArrayList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val database: ChaosflixDatabase,
    private val offlineItemManager: OfflineItemManager,
    private val preferencesManager: PreferencesManager,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    val state: SingleLiveEvent<LiveEvent<State, Bundle, String>> =
            SingleLiveEvent()

    private var waitingForRecordings = false

    val autoselectRecording: Boolean
        get() = preferencesManager.getAutoselectRecording()

    fun setEvent(event: Event): LiveData<Event?> {
        viewModelScope.launch {
            val recordings = mediaRepository.updateRecordingsForEvent(event)
            if (waitingForRecordings) {
                if (recordings != null) {
                    waitingForRecordings = false
                    val bundle = Bundle()
                    bundle.putParcelable(EVENT, event)
                    bundle.putParcelableArrayList(KEY_SELECT_RECORDINGS, ArrayList(recordings))
                    state.postValue(LiveEvent(State.SelectRecording, data = bundle))
                } else {
                    state.postValue(LiveEvent(State.Error, error = "Could not load recordings."))
                }
            }
        }
        return database.eventDao().findEventByGuid(event.guid)
    }

    fun getRecordingForEvent(event: Event): LiveData<List<Recording>> {
        viewModelScope.launch {
            mediaRepository.updateRecordingsForEvent(event)
        }
        return database.recordingDao().findRecordingByEvent(event.id)
    }

    fun getBookmarkForEvent(guid: String): LiveData<WatchlistItem?> =
            database.watchlistItemDao().getItemForEvent(guid)

    fun createBookmark(guid: String) = viewModelScope.launch(Dispatchers.IO) {
        database.watchlistItemDao().saveItem(WatchlistItem(eventGuid = guid))
    }

    fun removeBookmark(guid: String) = viewModelScope.launch(Dispatchers.IO) {
        database.watchlistItemDao().deleteItem(guid)
    }

    fun download(event: Event, recording: Recording) =
            offlineItemManager.download(event, recording)

    private suspend fun fileExists(guid: String): Boolean {
        val offlineItem = database.offlineEventDao().getByEventGuidSuspend(guid)
        return offlineItem != null && File(offlineItem.localPath).exists()
    }

    fun deleteOfflineItem(event: Event): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch {
            database.offlineEventDao().getByEventGuidSuspend(event.guid)?.let {
                offlineItemManager.deleteOfflineItem(it)
            }
            result.postValue(true)
        }
        return result
    }

    fun getRelatedEvents(event: Event): LiveData<List<Event>> = mediaRepository.getReleatedEvents(event)

    fun relatedEventSelected(event: Event) {
        val bundle = Bundle()
        bundle.putParcelable(EVENT, event)
        state.postValue(LiveEvent(State.DisplayEvent, data = bundle))
    }

    fun playEvent(event: Event) {
        viewModelScope.launch(Dispatchers.IO) {
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
                val items: List<Recording> = database.recordingDao().findRecordingByEventSync(event.id)
                if (items.isNotEmpty()) {
                    val bundle = Bundle()
                    bundle.putParcelable(EVENT, event)
                    bundle.putParcelableArrayList(KEY_SELECT_RECORDINGS, ArrayList(items))
                    state.postValue(LiveEvent(State.SelectRecording, data = bundle))
                } else {
                    state.postValue(LiveEvent(State.Loading))
                    waitingForRecordings = true
                }
            }
        }
    }

    private fun playRecording(event: Event, recording: Recording, urlForThumbs: String? = null) = viewModelScope.launch {
        val bundle = Bundle().apply {
            putParcelable(RECORDING, recording)
            putParcelable(EVENT, event)
            putString(THUMBS_URL, urlForThumbs)
        }
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
        viewModelScope.launch(Dispatchers.IO) {
            val items = database.recordingDao().findRecordingByEventSync(e.id)
            val bundle = Bundle()
            bundle.putParcelable(EVENT, e)
            bundle.putParcelableArrayList(KEY_SELECT_RECORDINGS, ArrayList(items))
            state.postValue(LiveEvent(s, bundle))
        }
    }

    fun play(event: Event) = viewModelScope.launch {
        if (autoselectRecording) {
            val recordings = database.recordingDao().findRecordingByEventSync(event.id)
            val optimalRecording = ChaosflixUtil.getOptimalRecording(recordings, event.originalLanguage)
            val recordingUrl = ChaosflixUtil.getRecordingForThumbs(recordings)?.recordingUrl
            playRecording(event, optimalRecording, recordingUrl)
        } else {
            playEvent(event)
        }
    }

    fun recordingSelected(e: Event, r: Recording) {
        viewModelScope.launch {
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
        Loading
    }

    companion object {
        val TAG = DetailsViewModel::class.simpleName
        const val KEY_LOCAL_PATH = "local_path"
        const val KEY_SELECT_RECORDINGS = "select_recordings"
        const val RECORDING = "recording"
        const val EVENT = "event"
        const val THUMBS_URL = "thumbs_url"
    }
}
