package de.nicidienase.chaosflix.common.entities.recording.persistence

import android.arch.persistence.room.*
import de.nicidienase.chaosflix.common.entities.recording.Event
import de.nicidienase.chaosflix.common.entities.recording.Recording

@Entity(tableName = "event",
        foreignKeys = arrayOf(ForeignKey(
                entity = PersistentConference::class,
                parentColumns = (arrayOf("conferenceId")),
                childColumns = arrayOf("conferenceId"))))

open class PersistentEvent(@PrimaryKey(autoGenerate = false)
                           var eventId: Long = 0,
                           var conferenceId: Long = 0,
                           var guid: String = "",
                           var title: String = "",
                           var subtitle: String? = "",
                           var slug: String = "",
                           var link: String? = "",
                           var description: String? = "",
                           var originalLanguage: String = "",
                           var date: String? = "",
                           var releaseDate: String = "",
                           var updatedAt: String = "",
                           var length: Long = 0,
                           var thumbUrl: String = "",
                           var posterUrl: String = "",
                           var frontendLink: String? = "",
                           var url: String = "",
                           var conferenceUrl: String = "",
                           @Embedded
                           var metadata: Metadata? = null,

                           var isPromoted: Boolean = false,
                           var viewCount: Int = 0,

                           persons: Array<String>? = null,
                           tags: Array<String>? = null,
                           recordings: List<Recording>? = null
) {
//    @Relation(parentColumn = "eventId", entityColumn = "recordingId")
    @Ignore
    var recordings: List<PersistentRecording>? = recordings?.map { PersistentRecording(it) }

//    @Relation(parentColumn = "eventId", entityColumn = "personID")
    @Ignore
    var persons: List<Person>? = persons?.map { Person(it) }

//    @Relation(parentColumn = "eventId", entityColumn = "tagID")
    @Ignore
    var tags: List<Tag>? = tags?.map { Tag(it) }

    @Ignore
    constructor(event: Event) : this(event.eventID,
            event.conferenceId, event.guid, event.title,
            event.subtitle, event.slug, event.link, event.description,
            event.originalLanguage, event.date, event.releaseDate,
            event.updatedAt, event.length, event.thumbUrl, event.posterUrl,
            event.frontendLink, event.url, event.conferenceUrl,
            event.metadata, event.isPromoted, event.viewCount,
            event.persons, event.tags, event.recordings)

    fun toEvent(): Event = Event(conferenceId, guid, title, subtitle, slug, link, description,
            originalLanguage, persons?.map { it.person }?.toTypedArray(),
            tags?.map { it.tag }?.toTypedArray(), date, releaseDate, updatedAt, length,
            thumbUrl, posterUrl, frontendLink, url, conferenceUrl, recordings?.map { it.toRecording() }, metadata, isPromoted)
}