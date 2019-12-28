package de.nicidienase.chaosflix.common.eventimport

import com.google.gson.annotations.SerializedName

data class FahrplanLecture(
    var lectureId: String = "",
    var title: String,
    var subtitle: String = "",
    var day: Int = 0,
    var room: String? = null,
    var slug: String? = null,
    var url: String? = null,
    var startTime: Int = 0,
    var duration: Int = 0,
    var speakers: String? = null,
    var track: String? = null,
    var type: String? = null,
    var lang: String? = null,
    @SerializedName("abstractt")
    var abstract: String = "",
    var description: String = "",
    var relStartTime: Int = 0,
    var links: String? = null,
    var date: String? = null,
    var dateUTC: Long = 0,
    var roomIndex: Int = 0,
    var recordingLicense: String? = null,
    var recordingOptOut: Boolean = false
) {

    companion object {
        val RECORDING_OPTOUT_ON = true
        val RECORDING_OPTOUT_OFF = false
    }
}
