package de.nicidienase.chaosflix.touch.viewmodels

import android.arch.lifecycle.ViewModel
import de.nicidienase.chaosflix.common.entities.ChaosflixDatabase
import de.nicidienase.chaosflix.common.entities.recording.Conference
import de.nicidienase.chaosflix.common.entities.recording.ConferencesWrapper
import de.nicidienase.chaosflix.common.entities.recording.Event
import de.nicidienase.chaosflix.common.entities.recording.persistence.ConferenceGroup
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentConference
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentRecording
import de.nicidienase.chaosflix.common.entities.streaming.LiveConference
import de.nicidienase.chaosflix.common.entities.userdata.WatchlistItem
import de.nicidienase.chaosflix.common.network.RecordingService
import de.nicidienase.chaosflix.common.network.StreamingService
import de.nicidienase.chaosflix.touch.Util
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

/**
 * Created by felix on 12.10.17.
 */
class BrowseViewModel(
        val database: ChaosflixDatabase,
        val recordingApi: RecordingService,
        val streamingApi: StreamingService
) : ViewModel() {

    fun getConferencesMap(): Flowable<Map<String,List<PersistentConference>>>{
        updateConferencesAndGroups()
//            database.conferenceDao().getAllConferences()
//            .observeOn(Schedulers.io())
//            .map { ConferencesWrapper(it.map { it.toConference() }) }
//            .subscribeOn(Schedulers.io())
    }

    fun getConferenceGroups(): Flowable<ConferenceGroup> {
        updateConferencesAndGroups()
        return database.conferenceGroupDao().getAll()
    }

    private fun updateConferencesAndGroups() {
        recordingApi.getConferencesWrapper().observeOn(Schedulers.io()).subscribe { con: ConferencesWrapper? ->
            if (con != null) {
                con.conferenceMap.map { entry ->
                    val conferenceGroup: ConferenceGroup?
                            = database.conferenceGroupDao().getConferenceGroupByName(entry.key)
                    val groupId: Long
                    if (conferenceGroup != null) {
                        groupId = conferenceGroup.conferenceGroupId
                    } else {
                        val group = ConferenceGroup(entry.key)
                        val index = Util.orderedConferencesList.indexOf(group.name).toLong()
                        if (index != -1L)
                            group.index = index
                        groupId = database.conferenceGroupDao().addConferenceGroup(group)[0]
                    }
                    val conferenceList = entry.value
                            .map { PersistentConference(it) }
                            .map { it.conferenceGroupId = groupId; it }.toTypedArray()
                    database.conferenceDao().insertConferences(*conferenceList)
                }
            }
        }
    }

//            = recordingApi.getConferences().subscribeOn(Schedulers.io())

    fun getConferencesByGroup(group: String): Flowable<List<PersistentConference>>
            = database.conferenceGroupDao().getConferenceGroupByName(group)
            .flatMap { database.conferenceDao().findConferenceByGroup(it.conferenceGroupId) }
            .subscribeOn(Schedulers.io())

//            = recordingApi.getConferences().map { t: ConferencesWrapper -> t.getListForTag(group) }

    fun getConference(conferenceId: Long): Flowable<PersistentConference>
            = database.conferenceDao().findConferenceById(conferenceId).subscribeOn(Schedulers.io())
//            = recordingApi.getConference(conferenceId).subscribeOn(Schedulers.io())
//
//                { conference: Conference ->
//                    val persistentConference = PersistentConference(conference)
//                    val events = conference.events?.map { PersistentEvent(it) }
//                    conference.events?.map { it.persons?.map {  } }
//                })
//        return database.conferenceDao().findConferenceById(conferenceId)
//    }

    fun getEvent(apiID: Long): Flowable<PersistentEvent>
            = database.eventDao().findEventById(apiID).subscribeOn(Schedulers.io())
//            = recordingApi.getEvent(apiID).subscribeOn(Schedulers.io())

    fun getEventsforConference(conferenceId: Long): Flowable<PersistentEvent>
            = database.eventDao().findEventsByConference(conferenceId)

    fun getRecordingForEvent(id: Long): Flowable<List<PersistentRecording>>?
            = database.recordingDao().findRecordingByEvent(id)
            .subscribeOn(Schedulers.io())

    fun getStreamingConferences(): Observable<List<LiveConference>>
            = streamingApi.getStreamingConferences().subscribeOn(Schedulers.io())


    fun createBookmark(apiId: Long) {
        database.watchlistItemDao().getItemForEvent(apiId)
                .subscribe { watchlistItem: WatchlistItem? ->
                    if (watchlistItem == null) {
                        database.watchlistItemDao()
                                .saveItem(WatchlistItem(apiId, apiId))
                    }
                }
    }

    fun getBookmark(apiId: Long): Flowable<WatchlistItem> {
        return database.watchlistItemDao().getItemForEvent(apiId)
    }

    fun removeBookmark(apiID: Long) {
        getBookmark(apiID).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe { watchlistItem -> database.watchlistItemDao().deleteItem(watchlistItem) }
    }

    private val TAG: String = BrowseViewModel::class.simpleName.toString()

//    fun updateDatabase() {
//        recordingApi.getConferences()
//                .subscribeOn(Schedulers.io())
//                .observeOn(Schedulers.io())
//                .subscribe { conferenceWrapper: ConferencesWrapper ->
//                    // Get ConferencesWrapper and from it a list of ConferenceGroups
//                    val conGroups: List<ConferenceGroup>
//                            = conferenceWrapper.conferenceMap.map { entry -> ConferenceGroup(entry.key) }
//                    // Save conference-groups
//                    database.conferenceGroupDao().addConferenceGroup(*conGroups.toTypedArray())
//                    // Get saved conference-groups and save all conferences
//                    database.conferenceGroupDao().getAll()
//                            .subscribeOn(Schedulers.io())
//                            .observeOn(Schedulers.io())
//                            .subscribe { list: List<ConferenceGroup> ->
//                                // Iterate over saved conference-groups and save all conferences
//                                // belonging to them
//                                for (group in list) {
//                                    val groupList = conferenceWrapper.conferenceMap[group.name]
//                                    if (groupList != null) {
//                                        database.conferenceDao().insertConferences(*groupList
//                                                .map { PersistentConference(it) }
//                                                .map { it.conferenceGroupId = group.conferenceGroupId; it }
//                                                .toTypedArray())
//                                    }
//                                }
//                                // Load Events for all Conferences
//                                database.conferenceDao().getAllConferences()
//                                        .subscribeOn(Schedulers.io())
//                                        .observeOn(Schedulers.io())
//                                        .subscribe { conferences: List<PersistentConference> ->
//                                            updateEventsForConferences(conferences)
//                                        }
//                            }
//                }
//    }
//
//    private fun updateEventsForConferences(conferences: List<PersistentConference>) {
//        for (con in conferences) {
//            // Load all Events for one Conference
//            recordingApi.getConference(con.conferenceId)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(Schedulers.io())
//                    .subscribe { con: Conference ->
//                        updateEventsForConference(con)
//                    }
//        }
//    }
//
//    private fun updateEventsForConference(con: Conference) {
//        val events = con.events ?: emptyList()
//        val persistentEvents = events.map { PersistentEvent(it) }.toTypedArray()
//        database.eventDao().insertEvent(*persistentEvents)
//        database.eventDao().findEventsByConference(con.conferenceID)
//                .observeOn(Schedulers.io())
//                .subscribe({ events: List<PersistentEvent>? ->
//                    // Load Recordings for all Events
//                    updateRecordings(events)
//                })
//    }
//
//    private fun updateRecordings(events: List<PersistentEvent>?) {
//        for (event in events ?: emptyList()) {
//            recordingApi.getEvent(event.eventId)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(Schedulers.io())
//                    .subscribe { event: Event? ->
//                        val recordings = event?.recordings ?: emptyList()
//                        val persistentRecordings = recordings.map { PersistentRecording(it) }.toTypedArray()
//                        database.recordingDao().insertRecording(*persistentRecordings)
//                    }
//        }
//    }
}