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

    val state: SingleLiveEvent<LiveEvent<State, Bundle, String>> =
            SingleLiveEvent()

    private var waitingForRecordings = false

    var autoselectRecording: Boolean
        get() = preferencesManager.autoselectRecording
        set(value) { preferencesManager.autoselectRecording = value }

    var autoselectStream: Boolean
        get() = preferencesManager.autoselectStream
        set(value) { preferencesManager.autoselectStream = value }

    fun setEvent(event: Event): LiveData<Event?> {
        viewModelScope.launch(Dispatchers.IO) {
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

    fun play(event: Event, autoselect: Boolean = autoselectRecording) = viewModelScope.launch(Dispatchers.IO) {
        if (autoselect) {
            val recordings = database.recordingDao().findRecordingByEventSync(event.id)
            val optimalRecording = ChaosflixUtil.getOptimalRecording(recordings, event.originalLanguage)
            val recordingUrl = ChaosflixUtil.getRecordingForThumbs(recordings)?.recordingUrl
            playRecording(event, optimalRecording, recordingUrl)
        } else {
            playEvent(event)
        }
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

    fun deleteOfflineItem(event: Event): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch(Dispatchers.IO) {
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

    fun downloadRecordingForEvent(event: Event) =
            postStateWithEventAndRecordings(State.DownloadRecording, event)

    fun playInExternalPlayer(event: Event) = postStateWithEventAndRecordings(State.PlayExternal, event)


    fun recordingSelected(e: Event, r: Recording) {
        viewModelScope.launch(Dispatchers.IO) {
            val recordings: List<Recording> = database.recordingDao().findRecordingByEventSync(e.id)
            val url = ChaosflixUtil.getRecordingForThumbs(recordings)?.recordingUrl
            playRecording(e, r, url)
        }
    }

    private fun playEvent(event: Event) {
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
                if (preferencesManager.autoselectStream && items.isNotEmpty()) {
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

    private suspend fun fileExists(guid: String): Boolean {
        val offlineItem = database.offlineEventDao().getByEventGuidSuspend(guid)
        return offlineItem != null && File(offlineItem.localPath).exists()
    }

    private fun playRecording(event: Event, recording: Recording, urlForThumbs: String? = null) = viewModelScope.launch(Dispatchers.IO) {
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

    private fun postStateWithEventAndRecordings(s: State, e: Event) {
        viewModelScope.launch(Dispatchers.IO) {
            val items = database.recordingDao().findRecordingByEventSync(e.id)
            val bundle = Bundle()
            bundle.putParcelable(EVENT, e)
            bundle.putParcelableArrayList(KEY_SELECT_RECORDINGS, ArrayList(items))
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
        const val PROGRESS = "progress"
    }
}
