package de.nicidienase.chaosflix.common.entities.recording

import com.google.gson.annotations.SerializedName;


data class Conference(
        @SerializedName("acronym")            var acronym: String = "",
        @SerializedName("aspect_ratio")       var aspectRatio: String = "",
        @SerializedName("updated_at")         var updatedAt: String = "",
        @SerializedName("title")              var title: String = "",
        @SerializedName("schedule_url")       var scheduleUrl: String?,
        @SerializedName("slug")               var slug: String = "",
        @SerializedName("event_last_released_at") var lastReleaseAt: String = "",
        @SerializedName("webgen_location")    var webgenLocation: String = "",
        @SerializedName("logo_url")           var logoUrl: String = "",
        @SerializedName("images_url")         var imagesUrl: String = "",
        @SerializedName("recordings_url")     var recordingsUrl: String = "",
        @SerializedName("url")                var url: String = "",
        @SerializedName("events")             var events: List<Event>?

) : Comparable<Conference> {

    val conferenceID: Long
        get() = getIdFromUrl()

    val eventsByTags: Map<String, List<Event>>
        get() = getEventsMap(events)
    val sensibleTags: Set<String>
    val tagsUsefull: Boolean

    init {
        sensibleTags = getSensibleTags(eventsByTags.keys)
        tagsUsefull = sensibleTags.size > 0
    }

    private fun getEventsMap(events: List<Event>?): Map<String,List<Event>>{
                val map = HashMap<String, MutableList<Event>>()
                val untagged = ArrayList<Event>()
                if (events != null) {
                    for (event in events) {
                        if (event.tags?.isNotEmpty() ?: false) {
                            for (tag in event.tags!!) {
                                if (tag != null) {

                                    val list: MutableList<Event>
                                    if (map.keys.contains(tag)) {
                                        list = map[tag]!!
                                    } else {
                                        list = ArrayList<Event>()
                                        map.put(tag, list)
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
                        map.put("untagged", untagged)
                    }
                }
                return map
            }


    private fun getIdFromUrl(url: String = this.url): Long {
        val strings = url.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        try {
            return ((strings[strings.size - 1]).toLong())
        } catch (e: NumberFormatException){
            return 0
        }
    }

    private fun getSensibleTags(tags: Set<String>): Set<String>{
        val hashSet = HashSet<String>()
        for (s in tags) {
            if (!(acronym.equals(s) || s.matches(Regex.fromLiteral("\\d+")))) {
                hashSet.add(s)
            }
        }
        return hashSet
    }

    override fun compareTo(conference: Conference): Int {
        return slug.compareTo(conference.slug)
    }
}
