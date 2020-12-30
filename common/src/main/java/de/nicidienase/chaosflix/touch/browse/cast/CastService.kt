package de.nicidienase.chaosflix.touch.browse.cast

import android.app.Activity
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.StreamUrl
import de.nicidienase.chaosflix.common.userdata.entities.progress.PlaybackProgress
import de.nicidienase.chaosflix.common.userdata.entities.progress.PlaybackProgressDao
import de.nicidienase.chaosflix.common.util.SingleLiveEvent
import de.nicidienase.chaosflix.touch.browse.streaming.StreamingItem

abstract class CastService(
    val playbackProgressDao: PlaybackProgressDao
) {
    abstract val connected: Boolean
    val state: SingleLiveEvent<CastState> = SingleLiveEvent()

    abstract fun attachToActivity(activity: AppCompatActivity)
    abstract fun castStream(streamingItem: StreamingItem, streamUrl: StreamUrl, contentKey: String)
    abstract fun loadMediaAndPlay(recording: Recording, event: Event, progress: PlaybackProgress?)
    abstract fun addMediaRouteMenuItem(activity: Activity, menu: Menu)

    sealed class CastState {
        data class Error(val errorCode: Int): CastState()
    }
}
