package de.nicidienase.chaosflix.common.mediadata.entities.recording

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class RelatedEventDto(
    @SerializedName("event_guid") var eventGuid: String,
    @SerializedName("weight") var weight: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readInt()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(eventGuid)
        parcel.writeInt(weight)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RelatedEventDto> {
        override fun createFromParcel(parcel: Parcel): RelatedEventDto {
            return RelatedEventDto(parcel)
        }

        override fun newArray(size: Int): Array<RelatedEventDto?> {
            return arrayOfNulls(size)
        }
    }
}