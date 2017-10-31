package de.nicidienase.chaosflix.common.entities.recording

import android.arch.persistence.room.Entity
import android.os.Parcel
import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty


@Entity(tableName = "recording")
@JsonIgnoreProperties(ignoreUnknown = true)
open class Recording(
    val size: Int = 0,
    val length: Int = 0,
    @JsonProperty("mime_type")
    val mimeType: String,
    val language: String,
    val filename: String,
    val state: String,
    val folder: String,
    @JsonProperty("high_quality")
    val isHighQuality: Boolean = false,
    val width: Int = 0,
    val height: Int = 0,
    @JsonProperty("updated_at")
    val updatedAt: String,
    @JsonProperty("recording_url")
    val recordingUrl: String,
    val url: String,
    @JsonProperty("event_url")
    val eventUrl: String,
    @JsonProperty("conference_url")
    val conferenceUrl: String
) : Parcelable {

    val apiID: Long
    val parentEventID: Long

    init {
        val strings = url!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        apiID = (strings[strings.size - 1]).toLong()
        val split = eventUrl!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        parentEventID = (split[split.size - 1]).toLong()

    }

    protected constructor(`in`: Parcel) : this(
        `in`.readInt(),
        `in`.readInt(),
        `in`.readString(),
        `in`.readString(),
        `in`.readString(),
        `in`.readString(),
        `in`.readString(),
        `in`.readByte().toInt() != 0,
        `in`.readInt(),
        `in`.readInt(),
        `in`.readString(),
        `in`.readString(),
        `in`.readString(),
        `in`.readString(),
        `in`.readString()
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeInt(size)
        parcel.writeInt(length)
        parcel.writeString(mimeType)
        parcel.writeString(language)
        parcel.writeString(filename)
        parcel.writeString(state)
        parcel.writeString(folder)
        parcel.writeByte((if (isHighQuality) 1 else 0).toByte())
        parcel.writeInt(width)
        parcel.writeInt(height)
        parcel.writeString(updatedAt)
        parcel.writeString(recordingUrl)
        parcel.writeString(url)
        parcel.writeString(eventUrl)
        parcel.writeString(conferenceUrl)
    }

    companion object {

        val CREATOR: Parcelable.Creator<Recording> = object : Parcelable.Creator<Recording> {
            override fun createFromParcel(`in`: Parcel): Recording {
                return Recording(`in`)
            }

            override fun newArray(size: Int): Array<Recording?> {
                return arrayOfNulls(size)
            }
        }
    }

}
