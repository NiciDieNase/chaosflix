package de.nicidienase.chaosflix.common.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import de.nicidienase.chaosflix.common.ImportItem
import de.nicidienase.chaosflix.common.eventimport.FahrplanExport
import de.nicidienase.chaosflix.common.eventimport.FahrplanLecture
import de.nicidienase.chaosflix.common.mediadata.MediaRepository
import de.nicidienase.chaosflix.common.userdata.entities.watchlist.WatchlistItem
import de.nicidienase.chaosflix.common.userdata.entities.watchlist.WatchlistItemDao
import de.nicidienase.chaosflix.common.util.LiveEvent
import de.nicidienase.chaosflix.common.util.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoritesImportViewModel(
    private val watchlistItemDao: WatchlistItemDao,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    val state: SingleLiveEvent<LiveEvent<State, List<ImportItem>, Exception>> = SingleLiveEvent()

    fun handleLectures(string: String) {
        state.postValue(LiveEvent(State.WORKING))
        viewModelScope.launch(Dispatchers.IO) {
            val export = Gson().fromJson(string, FahrplanExport::class.java)
            val events: List<ImportItem>
            try {
                events = export.lectures.map { lecture: FahrplanLecture ->
                    val event = mediaRepository.findEventByTitle(lecture.title)
                    ImportItem(
                        lecture = lecture,
                        event = event,
                        selected = event != null
                    ) }
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
                if (item.selected && guid != null && watchlistItemDao.getItemForGuid(guid) == null) {
                    watchlistItemDao.saveItem(WatchlistItem(eventGuid = guid))
                }
            }
            state.postValue(LiveEvent(State.IMPORT_DONE))
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
