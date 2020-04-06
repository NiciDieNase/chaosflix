package de.nicidienase.chaosflix.common.util

import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording

object RecordingUtil {
    fun getStringForRecording(recording: Recording): String {
        return "${if (recording.isHighQuality) "HD" else "SD"}  ${recording.folder}  [${recording.language}]"
    }
}
