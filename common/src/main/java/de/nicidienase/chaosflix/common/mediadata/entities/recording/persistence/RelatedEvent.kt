package de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import de.nicidienase.chaosflix.common.mediadata.entities.recording.RelatedEventDto

@Entity(tableName = "related",
		foreignKeys = arrayOf(ForeignKey(
				entity = Event::class,
				onDelete = ForeignKey.CASCADE,
				parentColumns = arrayOf("id"),
				childColumns = arrayOf("parentEventId"))),
		indices = [Index("parentEventId","relatedEventGuid",unique = true)]
		)
data class RelatedEvent(
		@PrimaryKey(autoGenerate = true)
		var id: Long = 0,
		var parentEventId: Long,
		var relatedEventGuid: String,
		var weight: Int) : Parcelable {

	constructor(parcel: Parcel) : this(
			parcel.readLong(),
			parcel.readLong(),
			parcel.readString(),
			parcel.readInt()) {
	}

	@Ignore
	constructor(parentEventId: Long, relatedEvent: RelatedEventDto): this(
			parentEventId= parentEventId,
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