package de.nicidienase.chaosflix.common.mediadata.entities.streaming

import android.os.Parcel
import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

import java.util.ArrayList

/**
 * Created by felix on 23.03.17.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
data class Room (var slug: String,
                 var schedulename: String,
                 var thumb: String,
                 var link: String,
                 var display: String,
                 var streams: MutableList<Stream>): Parcelable{


    protected constructor(`in`: Parcel) : this(
        slug = `in`.readString(),
        schedulename = `in`.readString(),
        thumb = `in`.readString(),
        link = `in`.readString(),
        display = `in`.readString(),
        streams = `in`.createTypedArrayList(Stream.CREATOR))

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(slug)
        dest.writeString(schedulename)
        dest.writeString(thumb)
        dest.writeString(link)
        dest.writeString(display)
        dest.writeTypedList(streams)
    }


    companion object {

        val CREATOR: Parcelable.Creator<Room> = object : Parcelable.Creator<Room> {
            override fun createFromParcel(`in`: Parcel): Room {
                return Room(`in`)
            }

            override fun newArray(size: Int): Array<Room?> {
                return arrayOfNulls(size)
            }
        }

        val dummyObject: Room
            get() {
                val dummy = Room(
                        "dummy_room",
                        "Dummy Room",
                        "https://static.media.ccc.de/media/unknown.png",
                        "",
                        "Dummy Room",
                        ArrayList())
                dummy.streams.add(Stream.dummyObject)
                return dummy
            }
    }
}
