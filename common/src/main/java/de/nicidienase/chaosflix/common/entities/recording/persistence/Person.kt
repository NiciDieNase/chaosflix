package de.nicidienase.chaosflix.common.entities.recording.persistence

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "person", indices = arrayOf(Index(value = "person", unique = true)))
class Person (var person: String) {
    @PrimaryKey(autoGenerate = true)
    var personId: Long = 0
}