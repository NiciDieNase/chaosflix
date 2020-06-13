package de.nicidienase.chaosflix.common.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import de.nicidienase.chaosflix.common.AnalyticsWrapper
import de.nicidienase.chaosflix.common.mediadata.MediaRepository
import de.nicidienase.chaosflix.common.userdata.entities.progress.PlaybackProgress
import de.nicidienase.chaosflix.common.userdata.entities.progress.PlaybackProgressDao
import de.nicidienase.chaosflix.common.userdata.entities.watchlist.WatchlistItem
import de.nicidienase.chaosflix.common.userdata.entities.watchlist.WatchlistItemDao
import de.nicidienase.chaosflix.common.util.LiveEvent
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PreferencesViewModel(
    private val mediaRepository: MediaRepository,
    private val watchlistItemDao: WatchlistItemDao,
    private val progressItemDao: PlaybackProgressDao,
    private val exportDir: File,
    private val analyticsWrapper: AnalyticsWrapper
) : ViewModel() {
    private val gson = Gson()

    fun cleanNonUserData() {
        viewModelScope.launch(Dispatchers.IO) {
            mediaRepository.deleteNonUserData()
        }
    }

    fun startAnalytics() = analyticsWrapper.startAnalytics()

    fun stopAnalytics() = analyticsWrapper.stopAnalytics()

    fun exportFavorites() {
        viewModelScope.launch(Dispatchers.IO) {
            val favorites = watchlistItemDao.getAllSync()
            val json = gson.toJson(favorites)
            Log.d(TAG, json)
            writeJsonToFile(json, exportDir, FAVORITES_FILENAME)

            val progress = progressItemDao.getAllSync()
            val progressJson = gson.toJson(progress)
            Log.d(TAG, progressJson)
            writeJsonToFile(progressJson, exportDir, PROGRESS_FILENAME)
        }
    }

    private fun writeJsonToFile(json: String, exportDir: File, fileName: String) {
        if (exportDir.isDirectory) {
            val file = File("${exportDir.path}${File.separator}$fileName")
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

    private fun readJsonFromFile(exportDir: File, fileName: String): String? {
        return try {
            val file = File("${exportDir.path}${File.separator}$fileName")
            if (file.exists()) {
                val fileReader = FileReader(file)
                val bufferedReader = BufferedReader(fileReader)
                bufferedReader.readText()
            } else {
                null
            }
        } catch (ex: Exception) {
            Log.d(TAG, ex.message, ex)
            null
        }
    }

    fun importFavorites(): MutableLiveData<LiveEvent<State, List<WatchlistItem>, Exception>> {
        val mutableLiveData = MutableLiveData<LiveEvent<State, List<WatchlistItem>, Exception>>()
        mutableLiveData.postValue(LiveEvent(State.Loading, null, null))
        viewModelScope.launch(Dispatchers.IO) {
            val favoritesJson = readJsonFromFile(exportDir, FAVORITES_FILENAME)
            val progressJson = readJsonFromFile(exportDir, PROGRESS_FILENAME)
            if (favoritesJson == null && progressJson == null) {
                mutableLiveData.postValue(LiveEvent(State.Done, null, null))
            } else {
                try {
                    favoritesJson?.let {
                        val fromJson = gson.fromJson(it, Array<WatchlistItem>::class.java)
                        fromJson.map { watchlistItemDao.saveItem(it) }
                    }
                } catch (ex: Exception) {
                    Log.e(TAG, "bookmark import failed", ex)
                }
                try {
                    progressJson?.let {
                        val fromJson = gson.fromJson(it, Array<PlaybackProgress>::class.java)
                        fromJson.map { progressItemDao.saveProgress(it) }
                    }
                } catch (ex: Exception) {
                    Log.e(TAG, "progress import failed", ex)
                }
                mutableLiveData.postValue(LiveEvent(State.Done, error = null))
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
        private const val PROGRESS_FILENAME = "chaosflix_progress.json"
    }
}
