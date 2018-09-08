package de.nicidienase.chaosflix.touch.playback

import android.os.Parcel
import android.os.Parcelable

data class PlaybackItem (val title: String, val subtitle: String, val eventId: Long, val uri: String) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readLong(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(subtitle)
        parcel.writeLong(eventId)
        parcel.writeString(uri)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PlaybackItem> {
        override fun createFromParcel(parcel: Parcel): PlaybackItem {
            return PlaybackItem(parcel)
        }

        override fun newArray(size: Int): Array<PlaybackItem?> {
            return arrayOfNulls(size)
        }
    }

}