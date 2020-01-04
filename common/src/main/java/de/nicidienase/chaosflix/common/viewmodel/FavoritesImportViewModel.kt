package de.nicidienase.chaosflix.common.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import de.nicidienase.chaosflix.common.ImportItem
import de.nicidienase.chaosflix.common.eventimport.FahrplanExport
import de.nicidienase.chaosflix.common.eventimport.FahrplanLecture
import de.nicidienase.chaosflix.common.mediadata.MediaRepository
import de.nicidienase.chaosflix.common.userdata.entities.watchlist.WatchlistItem
import de.nicidienase.chaosflix.common.util.LiveEvent
import de.nicidienase.chaosflix.common.util.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoritesImportViewModel(
    private val mediaRepository: MediaRepository
) : ViewModel() {

    val state: SingleLiveEvent<LiveEvent<State, List<ImportItem>, String>> = SingleLiveEvent()

    private val _items = MutableLiveData<List<ImportItem>>()
    val items: LiveData<List<ImportItem>>
        get() = _items

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
                        event = event
                    ) }
                _items.postValue(events)
                state.postValue(LiveEvent(State.EVENTS_FOUND, events, null))
            } catch (e: Exception) {
                state.postValue(LiveEvent(State.ERROR, null, e.message))
            }
        }
    }

    fun import() {
        state.postValue(LiveEvent(State.WORKING))
        viewModelScope.launch(Dispatchers.IO) {
            val items: List<ImportItem> = _items.value?.filter { it.selected } ?: emptyList()
            if(!items.isEmpty()){
                for (item in items) {
                    Log.d(TAG, "${item.lecture.title}: ${item.selected}")
                    val guid = item.event?.guid
                    if (item.selected && guid != null) {
                        mediaRepository.saveOrUpdate(WatchlistItem(eventGuid = guid))
                    }
                }
                state.postValue(LiveEvent(State.IMPORT_DONE))
            } else {
                state.postValue(LiveEvent(State.ERROR, error = "No items to import"))
            }
        }
    }

    fun selectAll() {
        val items = _items.value
        items?.map { it.selected = it.event != null }
        _items.postValue(items)
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
