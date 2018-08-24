package de.nicidienase.chaosflix.common.entities.recording

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName;

open class RelatedEvent(
		@SerializedName("event_id") 	var eventId: Int,
		@SerializedName("event_guid") var eventGuid: String,
		@SerializedName("weight") var weight: Int) : Parcelable {

	constructor(parcel: Parcel) : this(
			parcel.readInt(),
			parcel.readString(),
			parcel.readInt()) {
	}

	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeInt(eventId)
		parcel.writeString(eventGuid)
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