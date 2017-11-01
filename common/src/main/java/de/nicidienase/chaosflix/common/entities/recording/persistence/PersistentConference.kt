package de.nicidienase.chaosflix.common.entities.recording.persistence

import android.arch.persistence.room.*
import de.nicidienase.chaosflix.common.entities.recording.Conference
import de.nicidienase.chaosflix.common.entities.recording.Event

@Entity(tableName = "conference")
open class PersistentConference(
        @PrimaryKey
        var conferenceId: Long = 0,
        var acronym: String = "",
        var aspectRatio: String = "",
        var title: String = "",
        var slug: String = "",
        var webgenLocation: String = "",
        var scheduleUrl: String? = "",
        var logoUrl: String = "",
        var imagesUrl: String = "",
        var recordingsUrl: String = "",
        var url: String = "",
        var updatedAt: String = ""
//        events: List<Event>? = null
) {
//    @Relation(parentColumn = "conferenceId", entityColumn = "eventId")
//    var events: List<PersistentEvent>?

//    init {
//        this.events = events?.map { PersistentEvent(it) }
//    }

    @Ignore
    constructor(con: Conference) : this(con.conferenceID,
            con.acronym, con.aspectRatio, con.title, con.slug, con.webgenLocation,
            con.scheduleUrl, con.logoUrl, con.imagesUrl, con.recordingsUrl, con.url,
            con.updatedAt)

    fun toConference() = Conference(acronym, aspectRatio, title, slug, webgenLocation,
            scheduleUrl, logoUrl, imagesUrl, recordingsUrl, url, updatedAt, null)
}