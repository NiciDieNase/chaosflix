package de.nicidienase.chaosflix.touch.browse.cast

import android.app.Activity
import android.util.Log
import android.view.Menu
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.StreamUrl
import de.nicidienase.chaosflix.touch.browse.streaming.StreamingItem
import pl.droidsonroids.casty.Casty
import pl.droidsonroids.casty.MediaData
import java.util.*

class CastService(val activity: Activity, withMiniController: Boolean = true) {

	private val casty: Casty = if (withMiniController) {
		Casty.create(activity).withMiniController()
	} else {
		Casty.create(activity)
	}
	val connected: Boolean
		get() = casty.isConnected

	private var remoteMediaClient: RemoteMediaClient? = null
	private var progressUpdater: UpdateSaver? = null

	init {
		casty.setOnCastSessionUpdatedListener { castSession ->
			remoteMediaClient = castSession.remoteMediaClient
			with(castSession.remoteMediaClient){
				remoteMediaClient = this
				progressUpdater?.let { this.addProgressListener(it, UPDATE_INTERVAL) }
			}
		}
	}

	fun castStream(streamingItem: StreamingItem, streamUrl: StreamUrl, contentKey: String) {
		val contentType = getContentTypeForKey(contentKey)
		val mediaData = MediaData.Builder(streamUrl.url)
				.setStreamType(MediaData.STREAM_TYPE_BUFFERED)
				.setContentType(contentType)
				.setTitle(streamingItem.conference.conference)
				.setSubtitle(streamingItem.room.display)
				.addPhotoUrl(streamingItem.room.thumb)
				.build()
		casty.player.loadMediaAndPlay(mediaData)
	}

	fun loadMediaAndPlay(recording: Recording, event: Event) {
		val mediaData = buildCastMediaData(recording, event)
		progressUpdater?.let { remoteMediaClient?.removeProgressListener(it) }
		progressUpdater = UpdateSaver(event)
		remoteMediaClient?.addProgressListener(progressUpdater, UPDATE_INTERVAL)
		casty.player.loadMediaAndPlay(mediaData)
	}

	fun addMediaRouteMenuItem(menu: Menu) {
		casty.addMediaRouteMenuItem(menu)
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

	inner class UpdateSaver(val event: Event) : RemoteMediaClient.ProgressListener {

		private val date = Date()

		override fun onProgressUpdated(progress: Long, duration: Long) {
			Log.i(TAG, "Progress: $progress of $duration, event-duration: ${event.length * 1000}")
//			progressDao.saveProgress(PlaybackProgress(event.id, event.guid, progress, date.time))
		}

	}

	companion object {
		val TAG = CastService::class.java.simpleName
		const val UPDATE_INTERVAL: Long = 2500
	}
}