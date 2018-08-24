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
				parentColumns = arrayOf("eventId"),
				childColumns = arrayOf("eventId"))))


class PersistentRelatedEvent( @PrimaryKey(autoGenerate = true)
							   val id: Long = 0,
							   val eventId: Long,
							   var relatedEventId: Int,
							   var relatedEventGuid: String,
							   var weight: Int): Parcelable {

	@Ignore
	constructor(parcel: Parcel) : this(
			parcel.readLong(),
			parcel.readLong(),
			parcel.readInt(),
			parcel.readString(),
			parcel.readInt())

	@Ignore
	constructor(eventId: Long, relatedEvent: RelatedEvent): this(
			eventId = eventId,
			relatedEventId = relatedEvent.eventId,
			relatedEventGuid = relatedEvent.eventGuid,
			weight = relatedEvent.weight)

	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeLong(id)
		parcel.writeLong(eventId)
		parcel.writeInt(relatedEventId)
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