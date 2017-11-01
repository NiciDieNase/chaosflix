package de.nicidienase.chaosflix.common.entities.recording.persistence

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "speaker_relation",
        foreignKeys = arrayOf(
            ForeignKey(
                entity = Person::class,
                parentColumns = arrayOf("personId"),
                childColumns = arrayOf("personId")),
            ForeignKey(
                    entity = PersistentEvent::class,
                    parentColumns = arrayOf("eventId"),
                    childColumns = arrayOf("eventId")
            )))
class SpeakerRelation(
        @PrimaryKey(autoGenerate = true)
        var speakerRelationId: Long = 0,
        var personId: Long,
        var eventId: Long
)