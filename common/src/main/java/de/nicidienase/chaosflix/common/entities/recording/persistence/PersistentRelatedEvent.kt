package de.nicidienase.chaosflix.common.entities.recording.persistence

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import de.nicidienase.chaosflix.common.entities.recording.RelatedEvent

@Entity(tableName = "related",
		foreignKeys = arrayOf(ForeignKey(
				entity = PersistentEvent::class,
				onDelete = ForeignKey.CASCADE,
				parentColumns = arrayOf("id"),
				childColumns = arrayOf("parentEventId"))))


class PersistentRelatedEvent(val parentEventId: Long,
                             var relatedEventGuid: String,
                             var weight: Int): PersistentItem(), Parcelable {

	constructor(parcel: Parcel) : this(
			parcel.readLong(),
			parcel.readString(),
			parcel.readInt()) {
	}

	@Ignore
	constructor(parentEventId: Long, relatedEvent: RelatedEvent): this(
			parentEventId= parentEventId,
			relatedEventGuid = relatedEvent.eventGuid,
			weight = relatedEvent.weight)

	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeLong(parentEventId)
		parcel.writeString(relatedEventGuid)
		parcel.writeInt(weight)
	}

	override fun describeContents(): Int {
		return 0
	}

	companion object CREATOR : Parcelable.Creator<PersistentRelatedEvent> {
		override fun createFromParcel(parcel: Parcel): PersistentRelatedEvent {
			return PersistentRelatedEvent(parcel)
		}

		override fun newArray(size: Int): Array<PersistentRelatedEvent?> {
			return arrayOfNulls(size)
		}
	}

}