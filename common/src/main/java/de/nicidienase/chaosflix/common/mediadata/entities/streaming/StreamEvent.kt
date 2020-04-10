package de.nicidienase.chaosflix.common.mediadata.entities.streaming

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.google.gson.annotations.SerializedName

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class StreamEvent(
    var title: String = "",
    var speaker: String = "",
    var fstart: String = "",
    var fend: String = "",
    var start: Long = 0,
    var end: Long = 0,
    var duration: Long = 0,
    @SerializedName("room_known")
    var roomKnown: Boolean = false
) : Parcelable {
    val description: String
    get() = "$title by $speaker"

    constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readLong(),
            parcel.readLong(),
            parcel.readLong(),
            parcel.readByte() != 0.toByte()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(speaker)
        parcel.writeString(fstart)
        parcel.writeString(fend)
        parcel.writeLong(start)
        parcel.writeLong(end)
        parcel.writeLong(duration)
        parcel.writeByte(if (roomKnown) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StreamEvent> {
        override fun createFromParcel(parcel: Parcel): StreamEvent {
            return StreamEvent(parcel)
        }

        override fun newArray(size: Int): Array<StreamEvent?> {
            return arrayOfNulls(size)
        }
    }
}
