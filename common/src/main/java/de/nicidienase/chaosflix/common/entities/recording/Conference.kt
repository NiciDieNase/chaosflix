package de.nicidienase.chaosflix.common.entities.recording

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable

import com.google.gson.annotations.SerializedName

import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@Entity(tableName = "conference")
class Conference(
    var acronym: String,
    @SerializedName("aspect_ratio")
    var aspectRation: String,
    var title: String,
    var slug: String,
    @SerializedName("webgen_location")
    var webgenLocation: String,
    @SerializedName("schedule_url")
    var scheduleUrl: String,
    @SerializedName("logo_url")
    var logoUrl: String,
    @SerializedName("images_url")
    var imagesUrl: String,
    @SerializedName("recordings_url")
    var recordingsUrl: String,
    var url: String,
    @SerializedName("updated_at")
    var updatedAt: String,
    var events: List<Event>
) : Parcelable, Comparable<Conference> {

    @PrimaryKey
    val apiID: Long
    val eventsByTags: HashMap<String, MutableList<Event>>

    init {
        eventsByTags = HashMap<String, MutableList<Event>>()
        val untagged = ArrayList<Event>()
        for (event in this.events) {
            if (event.tags.isNotEmpty()) {
                for (tag in event.tags) {
                    if (tag != null) {

                        val list: MutableList<Event>
                        if (eventsByTags.keys.contains(tag)) {
                            list = eventsByTags[tag]!!
                        } else {
                            list = ArrayList<Event>()
                            eventsByTags.put(tag, list)
                        }
                        list.add(event)
                    } else {
                        untagged.add(event)
                    }
                }
            } else {
                untagged.add(event)
            }
        }
        if (untagged.size > 0) {
            eventsByTags.put("untagged", untagged)
        }
        val strings = url.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        apiID = (strings[strings.size - 1]).toLong()
    }


    protected constructor(`in`: Parcel) : this(
        `in`.readString(),
        `in`.readString(),
        `in`.readString(),
        `in`.readString(),
        `in`.readString(),
        `in`.readString(),
        `in`.readString(),
        `in`.readString(),
        `in`.readString(),
        `in`.readString(),
        `in`.readString(),
        `in`.createTypedArrayList(Event.CREATOR)
    )

    override fun compareTo(conference: Conference): Int {
        return slug.compareTo(conference.slug)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeString(acronym)
        parcel.writeString(aspectRation)
        parcel.writeString(title)
        parcel.writeString(slug)
        parcel.writeString(webgenLocation)
        parcel.writeString(scheduleUrl)
        parcel.writeString(logoUrl)
        parcel.writeString(imagesUrl)
        parcel.writeString(recordingsUrl)
        parcel.writeString(url)
        parcel.writeString(updatedAt)
        parcel.writeTypedList(events)
    }

    companion object {

        val CREATOR: Parcelable.Creator<Conference> = object : Parcelable.Creator<Conference> {
            override fun createFromParcel(`in`: Parcel): Conference {
                return Conference(`in`)
            }

            override fun newArray(size: Int): Array<Conference?> {
                return arrayOfNulls(size)
            }
        }
    }
}
