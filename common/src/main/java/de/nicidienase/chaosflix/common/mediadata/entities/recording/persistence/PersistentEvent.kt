package de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence

import android.arch.persistence.room.*
import android.os.Parcel
import android.os.Parcelable
import android.text.Html
import android.text.Spanned
import de.nicidienase.chaosflix.common.mediadata.entities.recording.Event

@Entity(tableName = "event",
		foreignKeys = arrayOf(ForeignKey(
				entity = PersistentConference::class,
				onDelete = ForeignKey.CASCADE,
				parentColumns = (arrayOf("id")),
				childColumns = arrayOf("conferenceId"))),
		indices = arrayOf(Index("guid",unique = true), Index("frontendLink"), Index("conferenceId")))

data class PersistentEvent(
		var conferenceId: Long = 0,
		var conference: String = "",
		var guid: String = "",
		var title: String = "",
		var subtitle: String? = "",
		var slug: String = "",
		var link: String? = "",
		var description: String? = "",
		var originalLanguage: String = "",
		var date: String? = "",
		var releaseDate: String = "",
		var updatedAt: String = "",
		var length: Long = 0,
		var thumbUrl: String = "",
		var posterUrl: String = "",
		var frontendLink: String? = "",
		var url: String = "",
		var conferenceUrl: String = "",
		var isPromoted: Boolean = false,

		var viewCount: Int = 0,
		var persons: Array<String>? = null,

		var tags: Array<String>? = null,
		@Ignore
		var related: List<PersistentRelatedEvent>? = null,
		@Ignore
		var recordings: List<PersistentRecording>? = null
) : PersistentItem(), Parcelable {

	constructor(parcel: Parcel) : this(
			parcel.readLong(),
			parcel.readString(),
			parcel.readString(),
			parcel.readString(),
			parcel.readString(),
			parcel.readString(),
			parcel.readString(),
			parcel.readString(),
			parcel.readString(),
			parcel.readString(),
			parcel.readString(),
			parcel.readString(),
			parcel.readLong(),
			parcel.readString(),
			parcel.readString(),
			parcel.readString(),
			parcel.readString(),
			parcel.readString(),
			parcel.readByte() != 0.toByte(),
			parcel.readInt(),
			parcel.createStringArray(),
			parcel.createStringArray(),
			parcel.createTypedArrayList(PersistentRelatedEvent),
			parcel.createTypedArrayList(PersistentRecording))

	@Ignore
	constructor(event: Event,conferenceId: Long = 0) : this(
			conferenceId = conferenceId,
			guid = event.guid,
			title = event.title,
			subtitle = event.subtitle,
			slug = event.slug,
			link = event.link,
			description = event.description,
			originalLanguage = event.originalLanguage,
			date = event.date,
			releaseDate = event.releaseDate,
			updatedAt = event.updatedAt,
			length = event.length,
			thumbUrl = event.thumbUrl,
			frontendLink = event.frontendLink,
			url = event.url,
			conferenceUrl = event.conferenceUrl,
			isPromoted = event.isPromoted,
			viewCount = event.viewCount,
			persons = event.persons,
			tags = event.tags,
			related = event.related?.map { PersistentRelatedEvent(event.eventID,it) },
			recordings = event.recordings?.map { PersistentRecording(it) }
	)

	fun getExtendedDescription(): Spanned {
		val description = "$description\n\nreleased at: $releaseDate<br>Tags: ${tags?.joinToString(", ")}"
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
			return Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY)
		} else {
			@Suppress("DEPRECATION")
			return Html.fromHtml(description)
		}
	}

	fun getSpeakerString(): String?
			= persons?.joinToString(", ")

	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeLong(id)
		parcel.writeString(conference)
		parcel.writeString(guid)
		parcel.writeString(title)
		parcel.writeString(subtitle)
		parcel.writeString(slug)
		parcel.writeString(link)
		parcel.writeString(description)
		parcel.writeString(originalLanguage)
		parcel.writeString(date)
		parcel.writeString(releaseDate)
		parcel.writeString(updatedAt)
		parcel.writeLong(length)
		parcel.writeString(thumbUrl)
		parcel.writeString(posterUrl)
		parcel.writeString(frontendLink)
		parcel.writeString(url)
		parcel.writeString(conferenceUrl)
		parcel.writeByte(if (isPromoted) 1 else 0)
		parcel.writeInt(viewCount)
		parcel.writeStringArray(persons)
		parcel.writeStringArray(tags)
		parcel.writeTypedList(related)
		parcel.writeTypedList(recordings)
	}

	override fun describeContents(): Int {
		return 0
	}

	companion object CREATOR : Parcelable.Creator<PersistentEvent> {
		override fun createFromParcel(parcel: Parcel): PersistentEvent {
			return PersistentEvent(parcel)
		}

		override fun newArray(size: Int): Array<PersistentEvent?> {
			return arrayOfNulls(size)
		}
	}


}