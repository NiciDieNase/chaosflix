package de.nicidienase.chaosflix.common.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import de.nicidienase.chaosflix.common.eventimport.FahrplanExport
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.ConferenceDao
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.EventDao
import de.nicidienase.chaosflix.common.mediadata.network.FahrplanMappingService
import de.nicidienase.chaosflix.common.mediadata.sync.Downloader
import de.nicidienase.chaosflix.common.util.LiveEvent
import de.nicidienase.chaosflix.common.util.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoritesImportViewModel(
    private val conferenceDao: ConferenceDao,
    private val eventDao: EventDao,
    private val downloader: Downloader,
    private val mappingService: FahrplanMappingService
) : ViewModel() {

    val state: SingleLiveEvent<LiveEvent<State, List<Event>, Exception>> = SingleLiveEvent()

    fun handleUrls(urls: List<String>) {
        viewModelScope.launch {
            val events = urls.mapNotNull { eventDao.findEventByFahrplanUrl(it) }
            state.postValue(LiveEvent(State.EVENTS_FOUND, events, null))
        }
    }

    fun handleLectures(string: String) {
        state.postValue(LiveEvent(State.WORKING))
        viewModelScope.launch(Dispatchers.IO) {
            val export = Gson().fromJson(string, FahrplanExport::class.java)
            updateConferences(export.conference)
            val events: List<Event>
            try {
                events = export.lectures.mapNotNull { eventDao.findEventByTitleSuspend(it.title) }
                state.postValue(LiveEvent(State.EVENTS_FOUND, events, null))
            } catch (e: Exception) {
                state.postValue(LiveEvent(State.ERROR, null, e))
            }
        }
    }

    private suspend fun updateConferences(conference: String) {
        val fahrplanMappings = mappingService.getFahrplanMappings()
        Log.d(TAG, "Updating conferences for $conference, mappings=$fahrplanMappings")
        if (fahrplanMappings.containsKey(conference)) {
            fahrplanMappings[conference]?.let { keys ->
                for (conf in keys) {
                    conferenceDao.findConferenceByAcronymSync(conf)?.let { conf ->
                        val list =
                            downloader.updateEventsForConferencesSuspending(conf)
                        Log.d(TAG, "updated ${conf.acronym}, got ${list.size} events")
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
        ERROR
    }

    companion object {
        private val TAG = FavoritesImportViewModel::class.java.simpleName
    }
}
