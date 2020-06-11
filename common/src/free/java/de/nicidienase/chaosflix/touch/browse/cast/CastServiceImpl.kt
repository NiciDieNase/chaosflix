package de.nicidienase.chaosflix.touch.browse.cast

import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.StreamUrl
import de.nicidienase.chaosflix.common.userdata.entities.progress.PlaybackProgress
import de.nicidienase.chaosflix.common.userdata.entities.progress.PlaybackProgressDao
import de.nicidienase.chaosflix.touch.browse.streaming.StreamingItem
import kotlinx.coroutines.CoroutineScope

@SuppressWarnings("unused")
class CastServiceImpl(
    playbackProgressDao: PlaybackProgressDao,
    scope: CoroutineScope
) : CastService(playbackProgressDao, scope) {

    override val connected: Boolean = false

    override fun castStream(streamingItem: StreamingItem, streamUrl: StreamUrl, contentKey: String) {
        Log.e(TAG, "No Cast Support")
    }

    override fun loadMediaAndPlay(recording: Recording, event: Event, progress: PlaybackProgress?) {
        Log.e(TAG, "No Cast Support")
    }

    override fun addMediaRouteMenuItem(menu: Menu) {
        Log.i(TAG, "No Cast Support, adding no Menu item")
    }

    override fun attachToActivity(activity: AppCompatActivity) {
        Log.i(TAG, "No Cast Support, doing nothing")
    }

    companion object {
        private val TAG = CastServiceImpl::class.java.simpleName + "Free"
    }
}
