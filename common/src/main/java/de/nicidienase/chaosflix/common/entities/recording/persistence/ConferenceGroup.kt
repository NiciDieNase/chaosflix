package de.nicidienase.chaosflix.common.entities.recording.persistence

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "conference_group", indices = arrayOf(Index(value = "name", unique = true)))
class ConferenceGroup{
    @PrimaryKey(autoGenerate = true)
    var conferenceGroupId: Long = 0
    var name: String = ""
}