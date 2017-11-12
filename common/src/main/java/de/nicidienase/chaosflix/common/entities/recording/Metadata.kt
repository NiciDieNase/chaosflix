package de.nicidienase.chaosflix.common.entities.recording

import android.os.Parcel
import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
open class Metadata(
        var related: Map<Long,Long>?,
        @JsonProperty("remote_id") var remoteId: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(null,parcel.readString()) {
        val mapSize = parcel.readInt()
        val map = HashMap<Long,Long>()
        for (i in 0..mapSize){
            map.put(parcel.readLong(),parcel.readLong())
        }
        related = map
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(remoteId)
        related?.run {
            parcel.writeInt(this.size)
            this.map { entry -> parcel.writeLong(entry.key); parcel.writeLong(entry.value) }
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Metadata> {
        override fun createFromParcel(parcel: Parcel): Metadata {
            return Metadata(parcel)
        }

        override fun newArray(size: Int): Array<Metadata?> {
            return arrayOfNulls(size)
        }
    }
}
