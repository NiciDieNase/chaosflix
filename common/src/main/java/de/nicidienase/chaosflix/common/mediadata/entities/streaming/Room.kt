package de.nicidienase.chaosflix.common.mediadata.entities.streaming

import android.os.Parcel
import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.*
import kotlin.collections.HashMap

@JsonIgnoreProperties(ignoreUnknown = true)
data class Room(var slug: String,
                var schedulename: String,
                var thumb: String,
                var link: String,
                var display: String,
                var talks: Map<String,StreamEvent>?,
                var streams: List<Stream>) : Parcelable {


	protected constructor(input: Parcel) : this(
			slug = input.readString() ?: "",
			schedulename = input.readString() ?: "",
			thumb = input.readString() ?: "",
			link = input.readString() ?: "",
			display = input.readString() ?: "",
			talks = readMap(input),
			streams = input.createTypedArrayList<Stream>(Stream.CREATOR).filterNotNull())


	override fun describeContents(): Int {
		return 0
	}

	override fun writeToParcel(dest: Parcel, flags: Int) {
		dest.writeString(slug)
		dest.writeString(schedulename)
		dest.writeString(thumb)
		dest.writeString(link)
		dest.writeString(display)
		val talkKeys = talks?.keys?.toTypedArray()
		dest.writeStringArray(talkKeys)
		val talks = talkKeys?.map { talks?.get(it) }?.toTypedArray()
		dest.writeTypedArray(talks,0)
		dest.writeTypedList(streams)
	}


	companion object CREATOR : Parcelable.Creator<Room> {


		override fun createFromParcel(`in`: Parcel): Room {
			return Room(`in`)
		}

		override fun newArray(size: Int): Array<Room?> {
			return arrayOfNulls(size)
		}

		fun readMap(input: Parcel): MutableMap<String, StreamEvent>? {
			val keys = input.createStringArray()
			val urls = input.createTypedArray(StreamEvent.CREATOR)
			if(keys == null || urls == null){
				return null
			}
			val result = HashMap<String, StreamEvent>()
			for(i in 0 until keys.size - 1){
				val key = keys[i]
				val value = urls[i]
				if(key != null && value != null){
					result.put(key, value)
				}
			}
			return result
		}

		val dummyObject: Room
			get() {
				val dummy = Room(
						"dummy_room",
						"Dummy Room",
						"https://static.media.ccc.de/media/unknown.png",
						"",
						"Dummy Room",
						HashMap(),
						ArrayList())
				dummy.streams = listOf(Stream.dummyObject)
				return dummy
			}

	}
}