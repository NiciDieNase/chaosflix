package de.nicidienase.chaosflix.common.mediadata.entities.streaming

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class Stream(
    var slug: String,
    var display: String,
    var type: String,
    var isTranslated: Boolean = false,
    var videoSize: IntArray?,
    var urls: MutableMap<String, StreamUrl>

) : Parcelable {

    protected constructor(input: Parcel) : this(
            slug = input.readString() ?: "",
            display = input.readString() ?: "",
            type = input.readString() ?: "",
            isTranslated = input.readByte().toInt() != 0,
            videoSize = input.createIntArray(),
            urls = readMap(input)
    )

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
        val urlKeys = urls.keys.toTypedArray()
        dest.writeStringArray(urlKeys)
        val urls = urlKeys.map { urls[it] }.toTypedArray()
        dest.writeTypedArray(urls, 0)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Stream

        if (slug != other.slug) return false
        if (display != other.display) return false
        if (type != other.type) return false
        if (isTranslated != other.isTranslated) return false
        if (videoSize != null) {
            videoSize?.let {
                if (other.videoSize == null) return false
                other.videoSize?.let { other ->
                    if (!it.contentEquals(other)) return false
                }
            }
        } else if (other.videoSize != null) return false
        if (urls != other.urls) return false

        return true
    }

    override fun hashCode(): Int {
        var result = slug.hashCode()
        result = 31 * result + display.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + isTranslated.hashCode()
        result = 31 * result + (videoSize?.contentHashCode() ?: 0)
        result = 31 * result + urls.hashCode()
        return result
    }

    companion object CREATOR : Parcelable.Creator<Stream> {
        override fun createFromParcel(parcel: Parcel): Stream {
            return Stream(parcel)
        }

        override fun newArray(size: Int): Array<Stream?> {
            return arrayOfNulls(size)
        }

        fun readMap(input: Parcel): MutableMap<String, StreamUrl> {
            val result = HashMap<String, StreamUrl>()
            val size = input.readInt()
            val keys = input.createStringArray() ?: emptyArray()
            val urls = input.createTypedArray(StreamUrl.CREATOR) ?: emptyArray()
            for (i in 0 until size) {
                val key = keys[i]
                val value = urls[i]
                if (key != null && value != null) {
                    result.put(key, value)
                }
            }
            return result
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
