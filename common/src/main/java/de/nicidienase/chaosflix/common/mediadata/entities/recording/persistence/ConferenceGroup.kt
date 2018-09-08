package de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence

import android.arch.persistence.room.*
import android.os.Parcel
import android.os.Parcelable

@Entity(
        tableName = "conference_group",
        indices = [Index(value = ["name"], unique = true)])
data class ConferenceGroup(
        var name: String = ""
): Parcelable {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    @ColumnInfo(name = "order_index")
    var index: Int = 1_000_000

    constructor(parcel: Parcel) : this(parcel.readString()) {
        index = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(index)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ConferenceGroup> {
        override fun createFromParcel(parcel: Parcel): ConferenceGroup {
            return ConferenceGroup(parcel)
        }

        override fun newArray(size: Int): Array<ConferenceGroup?> {
            return arrayOfNulls(size)
        }
    }

}