package de.nicidienase.chaosflix.common.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import de.nicidienase.chaosflix.common.AnalyticsWrapper
import de.nicidienase.chaosflix.common.AnalyticsWrapperImpl
import de.nicidienase.chaosflix.common.mediadata.MediaRepository
import de.nicidienase.chaosflix.common.userdata.entities.watchlist.WatchlistItem
import de.nicidienase.chaosflix.common.userdata.entities.watchlist.WatchlistItemDao
import de.nicidienase.chaosflix.common.util.LiveEvent
import de.nicidienase.chaosflix.common.util.ThreadHandler
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class PreferencesViewModel(
    private val mediaRepository: MediaRepository,
    private val watchlistItemDao: WatchlistItemDao,
    private val exportDir: File
) : ViewModel() {
    private val gson = Gson()

    private val threadHandler = ThreadHandler()

    private val analyticsWrapper: AnalyticsWrapper = AnalyticsWrapperImpl

    fun cleanNonUserData() {
        threadHandler.runOnBackgroundThread {
            mediaRepository.deleteNonUserData()
        }
    }

    fun startAnalytics() = analyticsWrapper.startAnalytics()

    fun stopAnalytics() = analyticsWrapper.stopAnalytics()

    fun exportFavorites() {
        threadHandler.runOnBackgroundThread {
            val favorites = watchlistItemDao.getAllSync()
            val json = gson.toJson(favorites)
            Log.d(TAG, json)
            if (exportDir.isDirectory) {
                val file = File("${exportDir.path}${File.separator}$FAVORITES_FILENAME")
                if (file.exists()) {
                    file.delete()
                    file.createNewFile()
                }
                try {
                    val fileWriter = FileWriter(file)
                    val bufferedWriter = BufferedWriter(fileWriter)
                    bufferedWriter.write(json)
                    bufferedWriter.close()
                    fileWriter.close()
                    Log.d(TAG, file.path)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    fun importFavorites(): MutableLiveData<LiveEvent<State, List<WatchlistItem>, Exception>> {
        val mutableLiveData = MutableLiveData<LiveEvent<State, List<WatchlistItem>, Exception>>()
        mutableLiveData.postValue(LiveEvent(State.Loading, null, null))
        threadHandler.runOnBackgroundThread {
            val file = File("${exportDir.path}${File.separator}$FAVORITES_FILENAME")
            try {
                if (file.exists()) {
                    val fileReader = FileReader(file)
                    val bufferedReader = BufferedReader(fileReader)
                    val json: String = bufferedReader.readText()
                    val fromJson = gson.fromJson(json, Array<WatchlistItem>::class.java)
                    fromJson.map { watchlistItemDao.saveItem(WatchlistItem(eventGuid = it.eventGuid)) }
                    mutableLiveData.postValue(LiveEvent(State.Done, fromJson.asList(), null))
                } else {
                    mutableLiveData.postValue(LiveEvent(State.Done, null, null))
                }
            } catch (ex: Exception) {
                mutableLiveData.postValue(LiveEvent(State.Done, null, ex))
            }
        }
        return mutableLiveData
    }

    enum class State {
        Loading, Done
    }

    companion object {
        private val TAG = PreferencesViewModel::class.java.simpleName
        private const val FAVORITES_FILENAME = "chaosflix_favorites.json"
    }
}