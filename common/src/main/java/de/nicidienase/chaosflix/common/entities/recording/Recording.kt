package de.nicidienase.chaosflix.common.entities.recording

import android.os.Parcel
import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty


@JsonIgnoreProperties(ignoreUnknown = true)
open class Recording(
    var size: Int = 0,
    var length: Int = 0,
    @JsonProperty("mime_type")
    var mimeType: String = "",
    var language: String = "",
    var filename: String = "",
    var state: String = "",
    var folder: String = "",
    @JsonProperty("high_quality")
    var isHighQuality: Boolean = false,
    var width: Int = 0,
    var height: Int = 0,
    @JsonProperty("updated_at")
    var updatedAt: String = "",
    @JsonProperty("recording_url")
    var recordingUrl: String = "",
    var url: String = "",
    @JsonProperty("event_url")
    var eventUrl: String = "",
    @JsonProperty("conference_url")
    var conferenceUrl: String = ""
) {

    var recordingID: Long
    var eventID: Long

    init {
        val strings = url.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        recordingID = (strings[strings.size - 1]).toLong()
        val split = eventUrl.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        eventID = (split[split.size - 1]).toLong()
    }
}
