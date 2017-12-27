package de.nicidienase.chaosflix.common.entities.streaming

import android.arch.persistence.room.Entity

import java.util.ArrayList

@Entity(tableName = "group")
data class Group(
    var group: String,
    var rooms: MutableList<Room>){


    companion object {
        val dummyObject: Group
            get() {
                val dummy = Group("Dummy Group", ArrayList<Room>())
                dummy.rooms.add(Room.dummyObject)
                return dummy
            }
    }
}
