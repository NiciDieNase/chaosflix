package de.nicidienase.chaosflix.touch.browse.cast

import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.StreamUrl
import de.nicidienase.chaosflix.touch.browse.streaming.StreamingItem
import pl.droidsonroids.casty.Casty
import pl.droidsonroids.casty.MediaData

class CastService() : LifecycleObserver {

    private var casty: Casty? = null
    val connected: Boolean
        get() = casty?.isConnected ?: false

    fun attachToActivity(activity: AppCompatActivity, withMiniController: Boolean = false) {
        casty = if (withMiniController) {
            Casty.create(activity).withMiniController()
        } else {
            Casty.create(activity)
        }
        activity.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        casty = null
    }

    fun castStream(streamingItem: StreamingItem, streamUrl: StreamUrl, contentKey: String) {
        casty?.let {
            val contentType = getContentTypeForKey(contentKey)
            val mediaData = MediaData.Builder(streamUrl.url)
                    .setStreamType(MediaData.STREAM_TYPE_BUFFERED)
                    .setContentType(contentType)
                    .setTitle(streamingItem.conference.conference)
                    .setSubtitle(streamingItem.room.display)
                    .addPhotoUrl(streamingItem.room.thumb)
                    .build()
            it.player?.loadMediaAndPlay(mediaData)
        }
    }

    fun loadMediaAndPlay(recording: Recording, event: Event) {
        casty?.let {
            val mediaData = buildCastMediaData(recording, event)
            it.player?.loadMediaAndPlay(mediaData)
        }
    }

    fun addMediaRouteMenuItem(menu: Menu) {
        casty?.addMediaRouteMenuItem(menu)
    }

    private fun getContentTypeForKey(s: String): String? {
        return when (s) {
            "hls" -> "application/x-mpegurl"
            "webm" -> "video/webm"
            "mp3" -> "audio/mp3"
            "opus" -> "audio/webm"
            "dash" -> "application/dash+xml"
            else -> ""
        }
    }

    private fun buildCastMediaData(recording: Recording, event: Event): MediaData {
        return MediaData.Builder(recording.recordingUrl)
            .setStreamType(MediaData.STREAM_TYPE_BUFFERED)
            .setContentType(recording.mimeType)
            .setTitle(event.title)
            .setSubtitle(event.subtitle)
            .addPhotoUrl(event.thumbUrl)
            .build()
    }
}
