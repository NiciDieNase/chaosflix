package de.nicidienase.chaosflix.common.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
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

    private var lastImport: String = ""

    private val _items = MutableLiveData<List<ImportItem>?>()
    val items: LiveData<List<ImportItem>?>
        get() = _items

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?>
        get() = _errorMessage

    private val _working = MutableLiveData<Boolean>(false)
    val working: LiveData<Boolean>
        get() = _working

    val importItemCount: LiveData<Int> = Transformations.map(items) { items ->
        val selectedItems = items?.filter { it.selected && it.event != null }
        return@map selectedItems?.count() ?: 0
    }

    val selectAll = MutableLiveData<Boolean>(true)

    fun handleLectures(jsonImport: String) {
        if (jsonImport == lastImport) {
            return
        }
        lastImport = jsonImport
        viewModelScope.launch(Dispatchers.IO) {
            loading {
                handleLecturesInternal(jsonImport)
            }
        }
    }

    internal suspend fun handleLecturesInternal(jsonImport: String) {
        val export = Gson().fromJson(jsonImport, FahrplanExport::class.java)
        val events: List<ImportItem>
        try {
            events = export.lectures.map { lecture: FahrplanLecture ->
                val event = mediaRepository.findEventByTitle(lecture.title)
                ImportItem(
                    lecture = lecture,
                    event = event,
                    selected = false
                ) }
            _items.postValue(events)
        } catch (e: Exception) {
            showErrorMessage(e.message ?: "An error occured while searching for recordings")
        }
    }

    fun importFavorites() {
        viewModelScope.launch(Dispatchers.IO) {
            val items: List<ImportItem> = _items.value?.filter { it.selected } ?: emptyList()
            if (items.isNotEmpty()) {
                loading {
                    for (item in items) {
                        Log.d(TAG, "${item.lecture.title}: ${item.selected}")
                        val guid = item.event?.guid
                        if (item.selected && guid != null) {
                            mediaRepository.saveOrUpdate(WatchlistItem(eventGuid = guid))
                        }
                    }
                    state.postValue(LiveEvent(State.IMPORT_DONE))
                }
            } else {
                showErrorMessage("No items to importFavorites")
            }
        }
    }

    fun selectAll(selected: Boolean? = null) {
        val items: List<ImportItem>? = _items.value
        val newList = items?.map { it.copy(selected = selected ?: (it.event != null)) }
        _items.postValue(newList)
    }

    fun itemChanged(item: ImportItem) {
        val items: MutableList<ImportItem>? = _items.value?.toMutableList()
        val index = items?.indexOf(item)
        if (index != null) {
            items[index] = items[index].copy()
        }
        _items.postValue(items)
    }

    fun selectNone() = selectAll(false)

    private suspend fun loading(work: suspend () -> Unit) {
        _working.postValue(true)
        work()
        _working.postValue(false)
    }

    fun errorShown() {
        _errorMessage.postValue(null)
    }

    private fun showErrorMessage(message: String) {
        val currentMessage = _errorMessage.value
        if (currentMessage != null) {
            _errorMessage.postValue("$currentMessage\n$message")
        } else {
            _errorMessage.postValue(message)
        }
    }

    enum class State {
        IMPORT_DONE
    }

    companion object {
        private val TAG = FavoritesImportViewModel::class.java.simpleName
    }
}
