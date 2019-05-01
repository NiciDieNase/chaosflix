package de.nicidienase.chaosflix.common.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.ChaosflixDatabase
import de.nicidienase.chaosflix.common.OfflineItemManager
import de.nicidienase.chaosflix.common.PreferencesManager
import de.nicidienase.chaosflix.common.ResourcesFacade
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.ConferenceGroup
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.LiveConference
import de.nicidienase.chaosflix.common.mediadata.network.RecordingService
import de.nicidienase.chaosflix.common.mediadata.network.StreamingService
import de.nicidienase.chaosflix.common.mediadata.sync.Downloader
import de.nicidienase.chaosflix.common.userdata.entities.download.OfflineEvent
import de.nicidienase.chaosflix.common.util.LiveDataMerger
import de.nicidienase.chaosflix.common.util.LiveEvent
import de.nicidienase.chaosflix.common.util.SingleLiveEvent
import de.nicidienase.chaosflix.common.util.ThreadHandler
import retrofit2.Response
import java.io.IOException

class BrowseViewModel(
    val offlineItemManager: OfflineItemManager,
    val database: ChaosflixDatabase,
    recordingApi: RecordingService,
    val streamingApi: StreamingService,
    val preferencesManager: PreferencesManager,
    private val resources: ResourcesFacade
) : ViewModel() {

    val state: SingleLiveEvent<LiveEvent<State, Event, String>> = SingleLiveEvent()

    enum class State {
        ShowEventDetails
    }

    val downloader = Downloader(recordingApi, database)
    private val handler = ThreadHandler()

    init {
        handler.runOnBackgroundThread {
            val downloadRefs =
                    database
                            .offlineEventDao()
                            .getAllSync()
                            .map { it.downloadReference }
            offlineItemManager.addDownloadRefs(downloadRefs)
        }
    }

    fun getConferenceGroups(): LiveData<List<ConferenceGroup>> =
            database.conferenceGroupDao().getAll()

    fun getConference(conferenceId: Long) =
            database.conferenceDao().findConferenceById(conferenceId)

    fun getConferencesByGroup(groupId: Long) =
            database.conferenceDao().findConferenceByGroup(groupId)

    fun getEventsforConference(conference: Conference) =
            database.eventDao().findEventsByConference(conference.id)

    fun getUpdateState() =
            downloader.updateConferencesAndGroups()

    fun updateEventsForConference(conference: Conference): LiveData<LiveEvent<Downloader.DownloaderState, List<Event>, String>> =
        LiveDataMerger<
                List<Event>,
                LiveEvent<Downloader.DownloaderState, List<Event>, String>,
                LiveEvent<Downloader.DownloaderState, List<Event>, String>>()
                .merge(getEventsforConference(conference),
                        downloader.updateEventsForConference(conference)) { list: List<Event>?, liveEvent: LiveEvent<Downloader.DownloaderState, List<Event>, String>? ->
                    return@merge LiveEvent(liveEvent?.state ?: Downloader.DownloaderState.DONE, list ?: liveEvent?.data, liveEvent?.error)
                }

    fun getBookmarkedEvents(): LiveData<List<Event>> = updateAndGetEventsForGuids {
        database
                .watchlistItemDao()
                .getAllSync().map { it.eventGuid } }

    fun getInProgressEvents(): LiveData<List<Event>> = updateAndGetEventsForGuids {
        database
                .playbackProgressDao()
                .getAllSync()
                .map { it.eventGuid } }

    fun getPromotedEvents(): LiveData<List<Event>> = database.eventDao().findPromotedEvents()

    private fun updateAndGetEventsForGuids(guidProvider: () -> List<String>): LiveData<List<Event>> {
        val result = MutableLiveData<List<Event>>()
        handler.runOnBackgroundThread {
            val guids = guidProvider.invoke()
            val events = guids.map { downloader.updateSingleEvent(it) }.filterNotNull()
            result.postValue(events)
        }
        return result
    }

    private val TAG = BrowseViewModel::class.simpleName

    fun getLivestreams(): LiveData<List<LiveConference>> {
        // TODO use LiveEvent for Result
        val result = MutableLiveData<List<LiveConference>>()
        handler.runOnBackgroundThread {
            val request: Response<List<LiveConference>>
            try {
                request = streamingApi.getStreamingConferences().execute()
            } catch (e: IOException) {
                result.postValue(emptyList())
                return@runOnBackgroundThread
            }
            if (!request.isSuccessful) {
                result.postValue(emptyList())
                return@runOnBackgroundThread
            }
            result.postValue(request.body())
        }
        return result
    }

    fun getOfflineEvents() = database.offlineEventDao().getAll()

    fun getOfflineDisplayEvents() = database.offlineEventDao().getOfflineEventsDisplay()

    fun mapOfflineEvents(offlineEvents: List<OfflineEvent>): List<Pair<OfflineEvent, Event>> {
        val offlineEventMap = offlineEvents.map { it.eventGuid to it }.toMap()
        val persistentEventMap = database.eventDao().findEventsByGUIDsSync(offlineEventMap.keys.toList())
                .map { it.guid to it }.toMap()

        val resultList = ArrayList<Pair<OfflineEvent, Event>>()
        for (key in offlineEventMap.keys) {
            val offlineEvent = offlineEventMap[key]
            var event: Event? = persistentEventMap[key]
            if (event == null) {
                event = downloader.updateSingleEvent(key)
            }
            if (event != null && offlineEvent != null) {
                resultList.add(Pair(offlineEvent, event))
            }
        }
        return resultList
    }

    fun getEventById(eventId: Long) = database.eventDao().findEventById(eventId)

    fun getRecordingByid(recordingId: Long) = database.recordingDao().findRecordingById(recordingId)

    fun updateDownloadStatus() {
        handler.runOnBackgroundThread {
            offlineItemManager.updateDownloadStatus(database.offlineEventDao().getAllSync())
        }
    }

    fun deleteOfflineItem(item: OfflineEvent) {
        handler.runOnBackgroundThread {
            offlineItemManager.deleteOfflineItem(item)
        }
    }

    fun showDetailsForEvent(guid: String) {
        handler.runOnBackgroundThread {
            val event = database.eventDao().findEventByGuidSync(guid)
            if (event != null) {
                state.postValue(LiveEvent(State.ShowEventDetails, event))
            } else {
                state.postValue(LiveEvent(State.ShowEventDetails, error = resources.getString(R.string.error_event_not_found)))
            }
        }
    }

    fun deleteOfflineItem(guid: String) {
        handler.runOnBackgroundThread {
            offlineItemManager.deleteOfflineItem(guid)
        }
    }
    fun getAutoselectStream() = preferencesManager.getAutoselectStream()
}
