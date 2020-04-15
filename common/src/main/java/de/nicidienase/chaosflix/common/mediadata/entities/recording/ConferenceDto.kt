package de.nicidienase.chaosflix.common.mediadata.entities.recording

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import de.nicidienase.chaosflix.common.util.ConferenceUtil

@Keep
data class ConferenceDto(
    @SerializedName("acronym") var acronym: String = "",
    @SerializedName("aspect_ratio") var aspectRatio: String = "",
    @SerializedName("updated_at") var updatedAt: String? = "",
    @SerializedName("title") var title: String = "",
    @SerializedName("schedule_url") var scheduleUrl: String? = null,
    @SerializedName("slug") var slug: String = "",
    @SerializedName("event_last_released_at") var lastReleaseAt: String? = "",
    @SerializedName("webgen_location") var webgenLocation: String? = "",
    @SerializedName("logo_url") var logoUrl: String = "",
    @SerializedName("images_url") var imagesUrl: String = "",
    @SerializedName("recordings_url") var recordingsUrl: String = "",
    @SerializedName("url") var url: String = "",
    @SerializedName("events") var events: List<EventDto>? = emptyList()

) : Comparable<ConferenceDto> {

    val conferenceID: Long
        get() = getIdFromUrl()

    private val eventsByTags: Map<String, List<EventDto>> by lazy { getEventsMap(events) }
    private val sensibleTags: Set<String>
    val tagsUsefull: Boolean

    init {
        sensibleTags = ConferenceUtil.getSensibleTags(eventsByTags.keys, acronym)
        tagsUsefull = sensibleTags.isNotEmpty()
    }

    private fun getEventsMap(events: List<EventDto>?): Map<String, List<EventDto>> {
        val map = HashMap<String, MutableList<EventDto>>()
        val untagged = ArrayList<EventDto>()
        if (events != null) {
            for (event in events) {
                if (event.tags?.isNotEmpty() == true) {
                    val tags = event.tags
                    for (tag: String in tags?.filterNotNull() ?: emptyList()) {

                        val list: MutableList<EventDto>
                        if (map.keys.contains(tag)) {
                            list = map[tag]!!
                        } else {
                            list = ArrayList()
                            map[tag] = list
                        }
                        list.add(event)
                    }
                } else {
                    untagged.add(event)
                }
            }
            if (untagged.size > 0) {
                map["untagged"] = untagged
            }
        }
        return map
    }

    private fun getIdFromUrl(url: String = this.url): Long {
        val strings = url.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        try {
            return ((strings[strings.size - 1]).toLong())
        } catch (e: NumberFormatException) {
            return 0
        }
    }

    override fun compareTo(other: ConferenceDto): Int {
        return slug.compareTo(other.slug)
    }
}
