package de.nicidienase.chaosflix.touch

import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import pl.droidsonroids.casty.MediaData

fun buildCastMediaData(recording: Recording, event: Event): MediaData {
	return MediaData.Builder(recording.recordingUrl)
			.setStreamType(MediaData.STREAM_TYPE_BUFFERED)
			.setContentType(recording.mimeType)
			.setTitle(event.title)
			.setSubtitle(event.subtitle)
			.addPhotoUrl(event.thumbUrl)
			.build()
}