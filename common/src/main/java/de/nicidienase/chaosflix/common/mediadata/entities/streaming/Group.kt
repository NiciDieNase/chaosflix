package de.nicidienase.chaosflix.common.mediadata.entities.streaming

import android.support.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class Group(
    var group: String,
    var rooms: MutableList<Room>
) {

    companion object {
        val dummyObject: Group
            get() {
                val dummy = Group("Dummy Group", mutableListOf())
                dummy.rooms.add(Room.dummyObject)
                return dummy
            }
    }
}
