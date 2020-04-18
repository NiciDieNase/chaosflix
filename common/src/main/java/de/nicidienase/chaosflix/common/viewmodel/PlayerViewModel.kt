package de.nicidienase.chaosflix.common.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.nicidienase.chaosflix.common.ChaosflixDatabase
import de.nicidienase.chaosflix.common.mediadata.ThumbnailParser
import de.nicidienase.chaosflix.common.userdata.entities.progress.PlaybackProgress
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerViewModel(private val database: ChaosflixDatabase, private val thumbnailParser: ThumbnailParser) : ViewModel() {

    private lateinit var eventGuid: String

    fun setEvent(guid: String) {
        eventGuid = guid
    }

    fun getPlaybackProgressLiveData(): LiveData<PlaybackProgress?> =
            database.playbackProgressDao().getProgressForEvent(eventGuid)

    suspend fun getPlaybackProgress(): PlaybackProgress? {
        return if (eventGuid.isNotBlank()) {
            database.playbackProgressDao().getProgressForEventSync(eventGuid)
        } else {
            null
        }
    }

    fun setPlaybackProgress(progress: Long) {
        if (progress < 5_000 || eventGuid.isBlank()) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            database.playbackProgressDao().saveProgress(
                    PlaybackProgress(
                            progress = progress,
                            eventGuid = eventGuid,
                            watchDate = Date().time))
        }
    }

    fun deletePlaybackProgress() {
        if (eventGuid.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                database.playbackProgressDao().deleteItem(eventGuid)
            }
        }
    }

    suspend fun getThumbInfo(): List<ThumbnailParser.ThumbnailInfo>? = withContext(Dispatchers.IO) {
        val event = database.eventDao().findEventByGuidSync(eventGuid)
        return@withContext if (event != null && event.thumbnailsUrl.isNotBlank()) {
            thumbnailParser.parse(event.thumbnailsUrl)
        } else {
            null
        }
    }
}
