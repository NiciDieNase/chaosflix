package de.nicidienase.chaosflix.common.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.ChaosflixDatabase
import de.nicidienase.chaosflix.common.OfflineItemManager
import de.nicidienase.chaosflix.common.PreferencesManager
import de.nicidienase.chaosflix.common.ResourcesFacade
import de.nicidienase.chaosflix.common.mediadata.MediaRepository
import de.nicidienase.chaosflix.common.mediadata.StreamingRepository
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.ConferenceGroup
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.LiveConference
import de.nicidienase.chaosflix.common.util.LiveDataMerger
import de.nicidienase.chaosflix.common.util.LiveEvent
import de.nicidienase.chaosflix.common.util.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BrowseViewModel(
    val offlineItemManager: OfflineItemManager,
    private val mediaRepository: MediaRepository,
    private val database: ChaosflixDatabase,
    private val streamingRepository: StreamingRepository,
    private val preferencesManager: PreferencesManager,
    private val resources: ResourcesFacade
) : ViewModel() {

    val state: SingleLiveEvent<LiveEvent<State, Event, String>> = SingleLiveEvent()

    enum class State {
        ShowEventDetails
    }

    init {
        viewModelScope.launch {
            val downloadRefs: List<Long> = mediaRepository.getAllOfflineEvents()
            offlineItemManager.addDownloadRefs(downloadRefs)
        }
    }

    fun getConferenceGroups(): LiveData<List<ConferenceGroup>> =
            database.conferenceGroupDao().getAll()

    fun getConferencesByGroup(groupId: Long) =
            database.conferenceDao().findConferenceByGroup(groupId)

    fun getEventsforConference(conference: Conference): LiveData<List<Event>> =
            database.eventDao().getEventsWithConferenceForConfernce(conference.id)

    fun getUpdateState() =
            mediaRepository.updateConferencesAndGroups()

    fun updateEventsForConference(conference: Conference): LiveData<LiveEvent<MediaRepository.State, List<Event>, String>> =
            LiveDataMerger<List<Event>, LiveEvent<MediaRepository.State, List<Event>, String>, LiveEvent<MediaRepository.State, List<Event>, String>>()
                    .merge(
                            getEventsforConference(conference),
                            mediaRepository.updateEventsForConference(conference)
                    ) { list: List<Event>?, liveEvent: LiveEvent<MediaRepository.State, List<Event>, String>? ->
                        return@merge LiveEvent(liveEvent?.state ?: MediaRepository.State.DONE, list
                                ?: liveEvent?.data, liveEvent?.error)
                    }

    fun getBookmarkedEvents(): LiveData<List<Event>> {
        val itemDao = database.watchlistItemDao()
        viewModelScope.launch(Dispatchers.IO) {
            itemDao.getAllSync().forEach {
                mediaRepository.updateSingleEvent(it.eventGuid)
            }
        }
        return itemDao.getWatchlistEvents()
    }

    @JvmOverloads
    fun getInProgressEvents(filterFinished: Boolean = false): LiveData<List<Event>> {
        val dao = database.playbackProgressDao()
        viewModelScope.launch(Dispatchers.IO) {
            dao.getAllSync().forEach {
                mediaRepository.updateSingleEvent(it.eventGuid)
            }
        }
        return Transformations.map(dao.getAllWithEvent()) { list ->
            return@map if (filterFinished) {
                val result = list.partition { it.progress.progress / 1000 + 10 < it.event?.length ?: 0 }
                Log.i(TAG, "Could not load events for ${list.filter { it.event == null }.map{it.progress.eventGuid}}")
                Log.i(TAG, "Filtered ${result.first.size} finished items: ${result.first.map { "${it.progress.progress / 1000}-${it.event?.length}|" }}")
                result.second.mapNotNull { it.event.apply { it.event?.progress = it.progress.progress } }
            } else {
                list.mapNotNull { it.event?.apply { it.event?.progress = it.progress.progress } }
            }
        }
    }

    fun getPromotedEvents(): LiveData<List<Event>> = database.eventDao().findPromotedEvents()

    fun getLivestreams(): LiveData<List<LiveConference>> = streamingRepository.streamingConferences

    fun updateLiveStreams() = viewModelScope.launch {
        streamingRepository.update()
    }

    fun getOfflineDisplayEvents() = database.offlineEventDao().getOfflineEventsDisplay()

    fun updateDownloadStatus() = viewModelScope.launch(Dispatchers.IO) {
        offlineItemManager.updateDownloadStatus(database.offlineEventDao().getAllSync())
    }

    fun showDetailsForEvent(guid: String) = viewModelScope.launch(Dispatchers.IO) {
        val event = database.eventDao().findEventByGuidSync(guid)
        if (event != null) {
            state.postValue(LiveEvent(State.ShowEventDetails, event))
        } else {
            state.postValue(LiveEvent(State.ShowEventDetails, error = resources.getString(R.string.error_event_not_found)))
        }
    }

    fun deleteOfflineItem(guid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            offlineItemManager.deleteOfflineItem(guid)
        }
    }

    fun getAutoselectStream() = preferencesManager.autoselectStream

    companion object {
        private val TAG = BrowseViewModel::class.simpleName
    }
}
