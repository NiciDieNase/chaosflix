package de.nicidienase.chaosflix.common.entities.recording

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@Entity(tableName = "conference")
@JsonIgnoreProperties(ignoreUnknown = true)
open class Conference(
        val acronym: String,
        @JsonProperty("aspect_ratio")
        val aspectRatio: String,
        val title: String,
        val slug: String,
        @JsonProperty("webgen_location") val webgenLocation: String,
        @JsonProperty("schedule_url") val scheduleUrl: String?,
        @JsonProperty("logo_url") val logoUrl: String,
        @JsonProperty("images_url") val imagesUrl: String,
        @JsonProperty("recordings_url")
        val recordingsUrl: String,
        val url: String,
        @JsonProperty("updated_at")
        val updatedAt: String,
        val events: List<Event>?
) : Parcelable, Comparable<Conference> {

    @PrimaryKey
    val apiID: Long
    val eventsByTags: HashMap<String, MutableList<Event>>

    val sensibleTags: MutableSet<String> = HashSet()

    init {
        eventsByTags = HashMap<String, MutableList<Event>>()
        val untagged = ArrayList<Event>()
        if(this.events != null){
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
        }
        val strings = url.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        apiID = (strings[strings.size - 1]).toLong()

        for (s in eventsByTags.keys) {
            if (!(acronym.equals(s) || s.matches(Regex.fromLiteral("\\d+")))) {
                sensibleTags.add(s)
            }
        }
    }

    fun areTagsUsefull(): Boolean = sensibleTags.size > 0

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
        parcel.writeString(aspectRatio)
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
