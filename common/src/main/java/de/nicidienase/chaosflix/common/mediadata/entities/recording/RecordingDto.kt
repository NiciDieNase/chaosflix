package de.nicidienase.chaosflix.common.mediadata.entities.recording

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class RecordingDto(
    var size: Int = 0,
    var length: Int = 0,
    @SerializedName("mime_type")
    var mimeType: String = "",
    var language: String = "",
    var filename: String = "",
    var state: String = "",
    var folder: String = "",
    @SerializedName("high_quality")
    var isHighQuality: Boolean = false,
    var width: Int = 0,
    var height: Int = 0,
    @SerializedName("updated_at")
    var updatedAt: String = "",
    @SerializedName("recording_url")
    var recordingUrl: String = "",
    var url: String = "",
    @SerializedName("event_url")
    var eventUrl: String = "",
    @SerializedName("conference_url")
    var conferenceUrl: String = ""
) {
    val recordingID: Long
    get() {
        val strings = url.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return (strings[strings.size - 1]).toLong()
    }

    fun getEventString(): String {
        val split = eventUrl.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return (split[split.size - 1])
    }
}
