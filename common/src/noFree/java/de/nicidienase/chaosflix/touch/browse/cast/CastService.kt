package de.nicidienase.chaosflix.touch.browse.cast

import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.Session
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.StreamUrl
import de.nicidienase.chaosflix.common.userdata.entities.progress.PlaybackProgress
import de.nicidienase.chaosflix.common.userdata.entities.progress.PlaybackProgressDao
import de.nicidienase.chaosflix.touch.browse.streaming.StreamingItem
import java.util.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.droidsonroids.casty.Casty
import pl.droidsonroids.casty.MediaData

class CastService(
    private val playbackProgressDao: PlaybackProgressDao,
    private val scope: CoroutineScope
) : LifecycleObserver {

    private var currentEvent: Event? = null

    private var casty: Casty? = null
    val connected: Boolean
        get() = casty?.isConnected ?: false

    fun attachToActivity(activity: AppCompatActivity) {
        casty = Casty.create(activity)
        activity.lifecycle.addObserver(this)
        currentEvent?.let {
            setupSessionListener(it)
        }
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

    fun loadMediaAndPlay(recording: Recording, event: Event, progress: PlaybackProgress?) {
        Log.d(TAG, "Casting: ${event.title}")
        currentEvent = event
        casty?.let {
            val mediaData = buildCastMediaData(recording, event, progress)
            it.player?.loadMediaAndPlay(mediaData)
        }
        setupSessionListener(event, progress)
    }

    private fun setupSessionListener(event: Event, progress: PlaybackProgress? = null) {
        val sessionListener = SessionListener(event.guid, progress)
        val sharedInstance = CastContext.getSharedInstance()
        val sessionManager = sharedInstance?.sessionManager
        sessionManager?.addSessionManagerListener(sessionListener as? SessionManagerListener<Session>)
        sessionManager?.currentCastSession?.let { sessionListener.attachProgressListener(it) }
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

    private fun buildCastMediaData(recording: Recording, event: Event, progress: PlaybackProgress?): MediaData {
        return MediaData.Builder(recording.recordingUrl)
            .setStreamType(MediaData.STREAM_TYPE_BUFFERED)
            .setContentType(recording.mimeType)
            .setTitle(event.title)
            .setSubtitle(event.subtitle)
            .addPhotoUrl(event.thumbUrl)
            .apply {
                progress?.progress?.let { setPosition(it) }
            }
            .build()
    }

    private inner class SessionListener(
        private val guid: String,
        progress: PlaybackProgress?
    ) : SessionManagerListener<CastSession> {

        private lateinit var playbackProgress: PlaybackProgress
        init {
            scope.launch {
                val progressFromDb = playbackProgressDao.getProgressForEventSync(guid)
                playbackProgress = when {
                    progress != null -> progress
                    progressFromDb != null -> progressFromDb
                    else -> {
                        val progress = PlaybackProgress(eventGuid = guid, progress = 0, watchDate = Date().time)
                        val id = playbackProgressDao.saveProgress(progress)
                        progress.apply { this.id = id }
                    }
                }
            }
        }

        val progressListener: RemoteMediaClient.ProgressListener = RemoteMediaClient.ProgressListener { l, l2 ->
            if (l > 0) {
                playbackProgress.progress = l
                playbackProgress.date = Date()
                scope.launch(Dispatchers.IO) {
                    playbackProgressDao.updateProgress(playbackProgress)
                }
                Log.d(TAG, "Progress ($guid): $l $l2 ($playbackProgress)")
            }
        }

        fun attachProgressListener(castSession: CastSession) {

            Log.d(TAG, "Adding Progress Listener")
            castSession.remoteMediaClient?.removeProgressListener(progressListener)
            castSession.remoteMediaClient?.addProgressListener(progressListener, 5000)
        }

        override fun onSessionStarted(p0: CastSession?, p1: String?) {
            Log.d(TAG, "onSessionStarted")
            p0?.let { attachProgressListener(it) }
        }

        override fun onSessionResumeFailed(p0: CastSession?, p1: Int) {
        }

        override fun onSessionSuspended(p0: CastSession?, p1: Int) {
        }

        override fun onSessionEnded(p0: CastSession?, p1: Int) {
        }

        override fun onSessionResumed(p0: CastSession?, p1: Boolean) {
            Log.d(TAG, "onSessionResumed")
            p0?.let { attachProgressListener(it) }
        }

        override fun onSessionStarting(p0: CastSession?) {
        }

        override fun onSessionResuming(p0: CastSession?, p1: String?) {
        }

        override fun onSessionEnding(p0: CastSession?) {
        }

        override fun onSessionStartFailed(p0: CastSession?, p1: Int) {
        }
    }

    companion object {
        private val TAG = CastService::class.java.simpleName
    }
}
