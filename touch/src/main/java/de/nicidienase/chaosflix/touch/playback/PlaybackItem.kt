package de.nicidienase.chaosflix.touch.playback

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.StreamUrl

@Keep
data class PlaybackItem(val title: String, val subtitle: String, val eventGuid: String, val uri: String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "") {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(subtitle)
        parcel.writeString(eventGuid)
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

        fun fromEvent(event: Event, recordingUri: String? = null, recording: Recording? = null): PlaybackItem {
            return PlaybackItem(
                    event.title,
                    event.subtitle ?: "",
                    event.guid,
                    recordingUri ?: recording?.recordingUrl ?: "")
        }

        fun fromStream(conference: String, display: String, streamUrl: StreamUrl): PlaybackItem {
            return PlaybackItem(conference, display, "", streamUrl.url)
        }
    }
}
