package de.nicidienase.chaosflix.touch.viewmodels

import android.arch.lifecycle.ViewModel
import de.nicidienase.chaosflix.common.entities.ChaosflixDatabase
import de.nicidienase.chaosflix.common.entities.recording.ConferencesWrapper
import de.nicidienase.chaosflix.common.entities.recording.persistence.ConferenceGroup
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentConference
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentRecording
import de.nicidienase.chaosflix.common.entities.userdata.WatchlistItem
import de.nicidienase.chaosflix.common.network.RecordingService
import de.nicidienase.chaosflix.common.network.StreamingService
import de.nicidienase.chaosflix.touch.Util
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

/**
 * Created by felix on 12.10.17.
 */
class BrowseViewModel(
        val database: ChaosflixDatabase,
        val recordingApi: RecordingService,
        val streamingApi: StreamingService
) : ViewModel() {

    fun getConferenceGroups(): Flowable<List<ConferenceGroup>> {
        updateConferencesAndGroups()
        return database.conferenceGroupDao().getAll()
    }

    private fun updateConferencesAndGroups() {
        recordingApi.getConferencesWrapper()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe { con: ConferencesWrapper? ->
            if (con != null) {
                con.conferenceMap.map { entry ->
                    val conferenceGroup: ConferenceGroup?
                            = database.conferenceGroupDao().getConferenceGroupByName(entry.key)
                    val groupId: Long
                    if (conferenceGroup != null) {
                        groupId = conferenceGroup.conferenceGroupId
                    } else {
                        val group = ConferenceGroup(entry.key)
                        val index = Util.orderedConferencesList.indexOf(group.name)
                        if (index != -1)
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


    fun getConferencesByGroup(groupId: Long): Flowable<List<PersistentConference>>
            = database.conferenceDao().findConferenceByGroup(groupId)

    fun getConference(conferenceId: Long): Flowable<PersistentConference>
            = database.conferenceDao().findConferenceById(conferenceId).subscribeOn(Schedulers.io())

    fun getEventsforConference(conferenceId: Long): Flowable<PersistentEvent>
            = database.eventDao().findEventsByConference(conferenceId)

    fun getRecordingForEvent(id: Long): Single<List<PersistentRecording>>
            = database.recordingDao().findRecordingByEvent(id)

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
}