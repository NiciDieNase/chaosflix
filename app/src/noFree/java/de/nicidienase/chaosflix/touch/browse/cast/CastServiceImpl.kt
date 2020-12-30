package de.nicidienase.chaosflix.touch.browse.cast

import android.app.Activity
import android.net.Uri
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.gms.cast.CastStatusCodes
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.Session
import com.google.android.gms.cast.framework.SessionManager
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.images.WebImage
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.StreamUrl
import de.nicidienase.chaosflix.common.userdata.entities.progress.PlaybackProgress
import de.nicidienase.chaosflix.common.userdata.entities.progress.PlaybackProgressDao
import de.nicidienase.chaosflix.touch.browse.streaming.StreamingItem
import java.util.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CastServiceImpl(
    playbackProgressDao: PlaybackProgressDao
) : LifecycleObserver, CastService(playbackProgressDao) {

    private val sessionManagerListener: SessionManagerListener<CastSession> = SessionListener()
    private var sessionManager: SessionManager? = null
    private var currentSession: CastSession? = null

    private var currentEvent: Event? = null
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override val connected: Boolean
        get() = currentSession != null

    override fun attachToActivity(activity: AppCompatActivity) {
        val castContext = CastContext.getSharedInstance(activity)
        sessionManager = castContext.sessionManager
        currentSession = castContext.sessionManager.currentCastSession

        activity.lifecycle.addObserver(this)
        currentEvent?.let {
            setupSessionListener(it)
        }
        registerSessionManagerListener()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        currentSession = sessionManager?.currentCastSession
        registerSessionManagerListener()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        currentSession = null
        unregisterSessionManagerListener()
    }

    private fun registerSessionManagerListener() {
        sessionManager?.addSessionManagerListener(sessionManagerListener as? SessionManagerListener<Session>)
    }

    private fun unregisterSessionManagerListener() {
        sessionManager?.removeSessionManagerListener(sessionManagerListener as? SessionManagerListener<Session>)
    }

    override fun castStream(streamingItem: StreamingItem, streamUrl: StreamUrl, contentKey: String) {
        val loadRequestData = MediaLoadRequestData.Builder()
                .setMediaInfo(buildStreamMediaInfo(streamingItem, streamUrl, contentKey))
                .build()
        launchMediaLoadRequest(loadRequestData)
    }

    override fun loadMediaAndPlay(recording: Recording, event: Event, progress: PlaybackProgress?) {
        Log.d(TAG, "Casting: ${event.title}")
        currentEvent = event
        val loadRequestData = MediaLoadRequestData.Builder()
                .setMediaInfo(buildRecordingMediaInfo(recording, event))
                .apply {
                    if (progress != null) {
                        setCurrentTime(progress.progress)
                    }
                }
                .build()

        launchMediaLoadRequest(loadRequestData)

        setupSessionListener(event, progress)
    }

    private fun launchMediaLoadRequest(requestData: MediaLoadRequestData) {
        val load = currentSession?.remoteMediaClient?.load(requestData)
        load?.setResultCallback {
            Log.d(TAG, "Playing ${requestData.toJson()}")
            if (it.mediaError != null) {
                Log.d(TAG, "${it.mediaError.toJson()}")
                state.postValue(CastState.Error(it.mediaError.detailedErrorCode))
            }
        }
    }

    private fun setupSessionListener(event: Event, progress: PlaybackProgress? = null) {
        val sessionListener = ProgressListener(event.guid, progress)
        val sharedInstance = CastContext.getSharedInstance()
        val sessionManager = sharedInstance?.sessionManager
        sessionManager?.addSessionManagerListener(sessionListener as? SessionManagerListener<Session>)
        sessionManager?.currentCastSession?.let { sessionListener.attachProgressListener(it) }
    }

    override fun addMediaRouteMenuItem(activity: Activity, menu: Menu) {
        activity.menuInflater.inflate(R.menu.cast, menu)
        CastButtonFactory.setUpMediaRouteButton(activity, menu, R.id.media_route_menu_item)
    }

    private fun buildRecordingMediaInfo(recording: Recording, event: Event): MediaInfo {
        return MediaInfo.Builder(recording.recordingUrl)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(recording.mimeType)
                .setMetadata(buildMetaData(event.title, event.subtitle ?: "", event.thumbUrl))
                .setStreamDuration(event.length * 1000)
                .build()
    }

    private fun buildStreamMediaInfo(streamingItem: StreamingItem, streamUrl: StreamUrl, contentKey: String): MediaInfo {
        return MediaInfo.Builder(streamUrl.url)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(getContentTypeForKey(contentKey))
                .setMetadata(buildMetaData(streamingItem.room.display, streamingItem.conference.conference, streamingItem.room.thumb))
                .build()
    }

    private fun buildMetaData(title: String, subtitle: String, thumbUri: String): MediaMetadata {
        return MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE).apply {
            putString(MediaMetadata.KEY_TITLE, title)
            putString(MediaMetadata.KEY_SUBTITLE, subtitle)
            addImage(WebImage(Uri.parse(thumbUri)))
        }
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

    private inner class SessionListener() : SessionManagerListener<CastSession> {
        override fun onSessionStarting(p0: CastSession?) {
        }

        override fun onSessionStarted(p0: CastSession?, p1: String?) {
            currentSession = p0
        }

        override fun onSessionStartFailed(p0: CastSession?, p1: Int) {
            currentSession = null
        }

        override fun onSessionEnding(p0: CastSession?) {
        }

        override fun onSessionEnded(p0: CastSession?, p1: Int) {
            currentSession = null
        }

        override fun onSessionResuming(p0: CastSession?, p1: String?) {
        }

        override fun onSessionResumed(p0: CastSession?, p1: Boolean) {
            currentSession = p0
        }

        override fun onSessionResumeFailed(p0: CastSession?, p1: Int) {
            currentSession = null
        }

        override fun onSessionSuspended(p0: CastSession?, p1: Int) {
            currentSession = null
        }
    }

    private inner class ProgressListener(
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
            Log.d(TAG, "Cast session started")
            p0?.let { attachProgressListener(it) }
        }

        override fun onSessionStartFailed(p0: CastSession?, p1: Int) {
            Log.d(TAG, "Cast session start failed: ${CastStatusCodes.getStatusCodeString(p1)}")
        }

        override fun onSessionResumeFailed(p0: CastSession?, p1: Int) {
            Log.d(TAG, "Cast session resume failed: ${CastStatusCodes.getStatusCodeString(p1)}")
        }

        override fun onSessionSuspended(p0: CastSession?, p1: Int) {
            Log.d(TAG, "Cast session suspended")
        }

        override fun onSessionEnded(p0: CastSession?, p1: Int) {
            Log.d(TAG, "Cast session ended")
        }

        override fun onSessionResumed(p0: CastSession?, p1: Boolean) {
            Log.d(TAG, "Cast session resumed")
            p0?.let { attachProgressListener(it) }
        }

        override fun onSessionStarting(p0: CastSession?) {
            Log.d(TAG, "Cast session starting")
        }

        override fun onSessionResuming(p0: CastSession?, p1: String?) {
            Log.d(TAG, "Cast session resuming")
        }

        override fun onSessionEnding(p0: CastSession?) {
            Log.d(TAG, "Cast session ending")
        }
    }

    companion object {
        private val TAG = CastServiceImpl::class.java.simpleName + "NoFree"
    }
}
