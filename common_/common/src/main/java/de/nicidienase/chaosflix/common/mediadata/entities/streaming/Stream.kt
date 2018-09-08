package de.nicidienase.chaosflix.common.mediadata.entities.streaming

import android.os.Parcel
import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

import kotlin.collections.HashMap

@JsonIgnoreProperties(ignoreUnknown = true)
data class Stream(
        var slug: String,
        var display: String,
        var type: String,
        var isTranslated: Boolean = false,
        var videoSize: IntArray?,
        var urls: MutableMap<String, StreamUrl>

) : Parcelable {

    protected constructor(`in`: Parcel) : this(
            slug = `in`.readString(),
            display = `in`.readString(),
            type = `in`.readString(),
            isTranslated = `in`.readByte().toInt() != 0,
            videoSize = `in`.createIntArray(),
            urls = HashMap<String, StreamUrl>()
    ) {
        val mapSize = `in`.readInt()
        for (i in 0 until mapSize) {
            urls.put(`in`.readString(), `in`.readParcelable(StreamUrl::class.java.classLoader))
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(slug)
        dest.writeString(display)
        dest.writeString(type)
        dest.writeByte((if (isTranslated) 1 else 0).toByte())
        dest.writeIntArray(videoSize)
        dest.writeInt(urls.size)
        val keys = urls.keys
        for (s in keys) {
            dest.writeString(s)
            dest.writeParcelable(urls[s], 0)
        }
    }

     companion object CREATOR : Parcelable.Creator<Stream> {
        override fun createFromParcel(parcel: Parcel): Stream {
            return Stream(parcel)
        }

        override fun newArray(size: Int): Array<Stream?> {
            return arrayOfNulls(size)
        }

        private val MAP_KEY = "map-key"

        val dummyObject: Stream
            get() {
                val dummy = Stream("dummy", "Dummy", "video", false, intArrayOf(1, 1), HashMap())
                dummy.urls.put("hls", StreamUrl.getDummyObject("hls"))
                dummy.urls.put("dizzy", StreamUrl.getDummyObject("dizzy"))
                dummy.urls.put("webm,vp8", StreamUrl.getDummyObject("webm,vp8"))
                dummy.urls.put("mp4,h265", StreamUrl.getDummyObject("mp4,h265"))
                dummy.urls.put("4x3", StreamUrl.getDummyObject("4x3"))
                dummy.urls.put("winkekatze", StreamUrl.getDummyObject("winkekatze"))
                dummy.type = "dummy"
                return dummy
            }
    }
}
