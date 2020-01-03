package de.nicidienase.chaosflix.common.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import de.nicidienase.chaosflix.common.ImportItem
import de.nicidienase.chaosflix.common.eventimport.FahrplanExport
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.ConferenceDao
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.EventDao
import de.nicidienase.chaosflix.common.mediadata.network.FahrplanMappingService
import de.nicidienase.chaosflix.common.mediadata.sync.Downloader
import de.nicidienase.chaosflix.common.userdata.entities.watchlist.WatchlistItem
import de.nicidienase.chaosflix.common.userdata.entities.watchlist.WatchlistItemDao
import de.nicidienase.chaosflix.common.util.LiveEvent
import de.nicidienase.chaosflix.common.util.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoritesImportViewModel(
    private val conferenceDao: ConferenceDao,
    private val eventDao: EventDao,
    private val watchlistItemDao: WatchlistItemDao,
    private val downloader: Downloader,
    private val mappingService: FahrplanMappingService
) : ViewModel() {

    val state: SingleLiveEvent<LiveEvent<State, List<ImportItem>, Exception>> = SingleLiveEvent()

    fun handleLectures(string: String) {
        state.postValue(LiveEvent(State.WORKING))
        viewModelScope.launch(Dispatchers.IO) {
            val export = Gson().fromJson(string, FahrplanExport::class.java)
            val events: List<ImportItem>
            try {
                updateConferences(export.conference)
                events = export.lectures.mapNotNull { ImportItem(
                    lecture = it,
                    event = eventDao.findEventByTitleSuspend(it.title)) }
                state.postValue(LiveEvent(State.EVENTS_FOUND, events, null))
            } catch (e: Exception) {
                state.postValue(LiveEvent(State.ERROR, null, e))
            }
        }
    }

    fun import(events: List<ImportItem>) {
        state.postValue(LiveEvent(State.WORKING))
        viewModelScope.launch(Dispatchers.IO) {
            for (item in events) {
                Log.d(TAG, "${item.lecture.title}: ${item.selected}")
                val guid = item.event?.guid
                if (item.selected &&
                    guid != null &&
                    watchlistItemDao.getItemForGuid(guid) == null) {
                    watchlistItemDao.saveItem(WatchlistItem(eventGuid = guid))
                }
            }
            state.postValue(LiveEvent(State.IMPORT_DONE))
        }
    }

    private suspend fun updateConferences(conferenceName: String) {
        val fahrplanMappings = mappingService.getFahrplanMappings()
        Log.d(TAG, "Updating conferences for $conferenceName, mappings=$fahrplanMappings")
        if (fahrplanMappings.containsKey(conferenceName)) {
            fahrplanMappings[conferenceName]?.let { keys ->
                for (conferenceAcronym in keys) {
                    conferenceDao.findConferenceByAcronymSync(conferenceAcronym)?.let { conference ->
                        val list =
                            downloader.updateEventsForConferencesSuspending(conference)
                        Log.d(TAG, "updated ${conference.acronym}, got ${list.size} events")
                    }
                }
            }
        } else {
            Log.d(TAG, "Did not update any conferences")
        }
    }

    enum class State {
        WORKING,
        EVENTS_FOUND,
        IMPORT_DONE,
        ERROR
    }

    companion object {
        private val TAG = FavoritesImportViewModel::class.java.simpleName
    }
}
