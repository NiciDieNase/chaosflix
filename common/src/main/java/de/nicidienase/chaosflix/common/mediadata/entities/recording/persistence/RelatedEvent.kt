package de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import de.nicidienase.chaosflix.common.mediadata.entities.recording.RelatedEventDto

@Entity(tableName = "related",
        foreignKeys = arrayOf(ForeignKey(
                entity = Event::class,
                onDelete = ForeignKey.CASCADE,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("parentEventId"))),
        indices = [Index("parentEventId", "relatedEventGuid", unique = true)]
        )
data class RelatedEvent(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var parentEventId: Long,
    var relatedEventGuid: String,
    var weight: Int
) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readLong(),
            parcel.readString() ?: "",
            parcel.readInt()) {
    }

    @Ignore
    constructor(parentEventId: Long, relatedEvent: RelatedEventDto) : this(
            parentEventId = parentEventId,
            relatedEventGuid = relatedEvent.eventGuid,
            weight = relatedEvent.weight)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(parentEventId)
        parcel.writeString(relatedEventGuid)
        parcel.writeInt(weight)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RelatedEvent> {
        override fun createFromParcel(parcel: Parcel): RelatedEvent {
            return RelatedEvent(parcel)
        }

        override fun newArray(size: Int): Array<RelatedEvent?> {
            return arrayOfNulls(size)
        }
    }
}
