package de.nicidienase.chaosflix.common.entities.recording.persistence

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import de.nicidienase.chaosflix.common.entities.recording.Recording

@Entity(tableName = "recording",
        foreignKeys = arrayOf(ForeignKey(
            entity = PersistentEvent::class,
            parentColumns = (arrayOf("eventId")),
            childColumns = arrayOf("eventId"))))
open class PersistentRecording(
        @PrimaryKey
        var recordingId: Long,
        var eventId: Long,
        var size: Int = 0,
        var length: Int = 0,
        var mimeType: String = "",
        var language: String = "",
        var filename: String = "",
        var state: String = "",
        var folder: String = "",
        var isHighQuality: Boolean = false,
        var width: Int = 0,
        var height: Int = 0,
        var updatedAt: String = "",
        var recordingUrl: String = "",
        var url: String = "",
        var eventUrl: String = "",
        var conferenceUrl: String = ""
) {
    @Ignore
    constructor(rec: Recording) : this(rec.recordingID, rec.eventID,
            rec.size, rec.length, rec.mimeType,
            rec.language, rec.filename, rec.state, rec.folder, rec.isHighQuality,
            rec.width, rec.height, rec.updatedAt, rec.recordingUrl, rec.url,
            rec.eventUrl, rec.conferenceUrl)

    fun toRecording(): Recording = Recording(size, length, mimeType, language,
            filename, state, folder, isHighQuality, width, height, updatedAt,
            recordingUrl, url, eventUrl, conferenceUrl)
}