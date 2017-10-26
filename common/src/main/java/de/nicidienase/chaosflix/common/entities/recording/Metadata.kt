package de.nicidienase.chaosflix.common.entities.recording

import android.os.Parcel
import android.os.Parcelable

import com.google.gson.annotations.SerializedName

/**
 * Created by felix on 06.04.17.
 */

class Metadata(
        var related: LongArray,
        @SerializedName("remote_id") var remoteId: String) : Parcelable {

    protected constructor(`in`: Parcel) : this(
        related = `in`.createLongArray(),
        remoteId = `in`.readString())

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLongArray(related)
        dest.writeString(remoteId)
    }

    companion object {

        val CREATOR: Parcelable.Creator<Metadata> = object : Parcelable.Creator<Metadata> {
            override fun createFromParcel(`in`: Parcel): Metadata {
                return Metadata(`in`)
            }

            override fun newArray(size: Int): Array<Metadata?> {
                return arrayOfNulls(size)
            }
        }
    }
}
