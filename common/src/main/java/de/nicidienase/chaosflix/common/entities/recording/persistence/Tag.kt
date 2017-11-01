package de.nicidienase.chaosflix.common.entities.recording.persistence

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "tag" ,indices = arrayOf(Index(value = "tag", unique = true)))
class Tag (var tag: String){
    @PrimaryKey(autoGenerate = true)
    var tagID = 0
}