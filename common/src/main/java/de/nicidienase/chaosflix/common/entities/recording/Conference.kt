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

) : Comparable<Conference> {

    var conferenceID: Long

    val eventsByTags: HashMap<String, MutableList<Event>>

    val sensibleTags: MutableSet<String> = HashSet()

    var tagsUsefull: Boolean;

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
        tagsUsefull = sensibleTags.size > 0
    }

    override fun compareTo(conference: Conference): Int {
        return slug.compareTo(conference.slug)
    }
}
