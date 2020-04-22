package de.nicidienase.chaosflix.touch.browse.cast

import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.StreamUrl
import de.nicidienase.chaosflix.touch.browse.streaming.StreamingItem

@SuppressWarnings("unused")
class CastService {

    val connected: Boolean = false

    fun castStream(streamingItem: StreamingItem, streamUrl: StreamUrl, contentKey: String) {
        Log.e(TAG, "No Cast Support")
    }

    fun loadMediaAndPlay(recording: Recording, event: Event) {
        Log.e(TAG, "No Cast Support")
    }

    fun addMediaRouteMenuItem(menu: Menu) {
        Log.i(TAG, "No Cast Support, adding no Menu item")
    }

    fun attachToActivity(activity: AppCompatActivity) {
        Log.i(TAG, "No Cast Support, doing nothing")
    }

    companion object {
        val TAG = CastService::class.java.simpleName
    }
}
