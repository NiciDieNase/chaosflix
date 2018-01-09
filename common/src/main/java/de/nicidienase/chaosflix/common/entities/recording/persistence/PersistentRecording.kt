package de.nicidienase.chaosflix.common.entities.recording.persistence

import android.arch.persistence.room.*
import android.os.Parcel
import android.os.Parcelable
import de.nicidienase.chaosflix.common.entities.recording.Recording

@Entity(tableName = "recording",
		foreignKeys = arrayOf(ForeignKey(
				entity = PersistentEvent::class,
//				onDelete = ForeignKey.CASCADE,
				parentColumns = (arrayOf("eventId")),
				childColumns = arrayOf("eventId"))),
		indices = arrayOf(Index("eventId")))
data class PersistentRecording(
		@PrimaryKey
		var recordingId: Long,
		var eventId: Long,
		var size: Int = 0,
		var length: Int = 0,
		var mimeType: String = "",
		var language: String = "",
		var filename: String = "",
		var state: String = "",
		var folder: String = "",
		var isHighQuality: Boolean = false,
		var width: Int = 0,
		var height: Int = 0,
		var updatedAt: String = "",
		var recordingUrl: String = "",
		var url: String = "",
		var eventUrl: String = "",
		var conferenceUrl: String = ""
) : Parcelable {

	@Ignore
	constructor(rec: Recording) : this(rec.recordingID, rec.eventID,
			rec.size, rec.length, rec.mimeType,
			rec.language, rec.filename, rec.state, rec.folder, rec.isHighQuality,
			rec.width, rec.height, rec.updatedAt, rec.recordingUrl, rec.url,
			rec.eventUrl, rec.conferenceUrl)

	fun toRecording(): Recording = Recording(size, length, mimeType, language,
			filename, state, folder, isHighQuality, width, height, updatedAt,
			recordingUrl, url, eventUrl, conferenceUrl)

	@Ignore
	constructor(parcel: Parcel) : this(
			parcel.readLong(),
			parcel.readLong(),
			parcel.readInt(),
			parcel.readInt(),
			parcel.readString(),
			parcel.readString(),
			parcel.readString(),
			parcel.readString(),
			parcel.readString(),
			parcel.readByte() != 0.toByte(),
			parcel.readInt(),
			parcel.readInt(),
			parcel.readString(),
			parcel.readString(),
			parcel.readString(),
			parcel.readString(),
			parcel.readString()) {
	}

	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeLong(recordingId)
		parcel.writeLong(eventId)
		parcel.writeInt(size)
		parcel.writeInt(length)
		parcel.writeString(mimeType)
		parcel.writeString(language)
		parcel.writeString(filename)
		parcel.writeString(state)
		parcel.writeString(folder)
		parcel.writeByte(if (isHighQuality) 1 else 0)
		parcel.writeInt(width)
		parcel.writeInt(height)
		parcel.writeString(updatedAt)
		parcel.writeString(recordingUrl)
		parcel.writeString(url)
		parcel.writeString(eventUrl)
		parcel.writeString(conferenceUrl)
	}

	override fun describeContents(): Int {
		return 0
	}

	companion object CREATOR : Parcelable.Creator<PersistentRecording> {
		override fun createFromParcel(parcel: Parcel): PersistentRecording {
			return PersistentRecording(parcel)
		}

		override fun newArray(size: Int): Array<PersistentRecording?> {
			return arrayOfNulls(size)
		}
	}
}