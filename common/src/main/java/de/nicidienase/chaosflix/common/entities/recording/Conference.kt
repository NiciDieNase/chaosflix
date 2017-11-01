package de.nicidienase.chaosflix.common.entities.recording

import android.arch.persistence.room.Ignore
import android.os.Parcel
import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
open class Conference(
        var acronym: String = "",

        @JsonProperty("aspect_ratio")
        var aspectRatio: String = "",

        var title: String = "",

        var slug: String = "",

        @JsonProperty("webgen_location")
        var webgenLocation: String = "",

        @JsonProperty("schedule_url")
        var scheduleUrl: String? = "",

        @JsonProperty("logo_url")
        var logoUrl: String = "",

        @JsonProperty("images_url")
        var imagesUrl: String = "",

        @JsonProperty("recordings_url")
        var recordingsUrl: String = "",

        var url: String = "",

        @JsonProperty("updated_at")
        var updatedAt: String,

        var events: List<Event>?

) : Parcelable, Comparable<Conference> {

    var conferenceID: Long

    val eventsByTags: HashMap<String, MutableList<Event>>

    val sensibleTags: MutableSet<String> = HashSet()

    init {
        eventsByTags = HashMap<String, MutableList<Event>>()
        val untagged = ArrayList<Event>()
        val events = this.events
        if (events != null) {
            for (event in events) {
                if (event.tags?.isNotEmpty() ?: false) {
                    for (tag in event.tags!!) {
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
        conferenceID = (strings[strings.size - 1]).toLong()

        for (s in eventsByTags.keys) {
            if (!(acronym.equals(s) || s.matches(Regex.fromLiteral("\\d+")))) {
                sensibleTags.add(s)
            }
        }


    }

    fun areTagsUsefull(): Boolean = sensibleTags.size > 0

    @Ignore
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
