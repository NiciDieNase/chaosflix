package de.nicidienase.chaosflix.common.mediadata.entities.recording

import android.support.annotation.Keep
import kotlin.collections.HashMap

@Keep
data class ConferencesWrapper(var conferences: List<ConferenceDto>) {

    val conferencesMap: Map<String, List<ConferenceDto>>
        get() = generateConferencesMap()

    private fun generateConferencesMap(): HashMap<String, MutableList<ConferenceDto>> {
        val map = HashMap<String, MutableList<ConferenceDto>>()
        for (conference in conferences) {
            val split = conference.slug.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            when (split[0]) {
                CONGRESS ->
                    if (split[1].endsWith("sendezentrum")) {
                        getListForTag(map, "sendezentrum").add(conference)
                    } else {
                        getListForTag(map, CONGRESS).add(conference)
                    }
                CONFERENCES ->
                    when {
                        split.size >= 3 -> getListForTag(map, split[1]).add(conference)
                        else -> getListForTag(map, DEFAULT_CONFERENCE_GROUP).add(conference)
                    }
                EVENTS -> getListForTag(map, EVENT_GROUP).add(conference)
                ERFAS -> getListForTag(map, ERFA_GROUP).add(conference)
                DOCU -> getListForTag(map, DOCU_GROUP).add(conference)
                BROADCAST -> getListForTag(map, BROADCAST_GROUP).add(conference)
                else -> getListForTag(map, conference.slug).add(conference)
            }
        }
        val other = map[DEFAULT_CONFERENCE_GROUP]
        val removeList = ArrayList<String>()
        for (groupName in map.keys) {
            if (groupName != DEFAULT_CONFERENCE_GROUP) {
                val list = map[groupName]
                if (list != null) {
                    list.sortDescending()
                    if (list.size < MIN_NUM_CONS) {
                        other?.addAll(list)
                        removeList.add(groupName)
                    }
                }
            }
        }
        for (key in removeList) {
            map.remove(key)
        }
        return map
    }

    private fun getListForTag(map: MutableMap<String, MutableList<ConferenceDto>>, s: String): MutableList<ConferenceDto> {
        if (map.keys.contains(s)) {
            return map[s]!!
        } else {
            val list = ArrayList<ConferenceDto>()
            map.put(s, list)
            return list
        }
    }

    companion object {
        private const val CONGRESS = "congress"
        private const val EVENTS = "events"
        private const val CONFERENCES = "conferences"
        private const val ERFAS = "erfas"
        private const val DOCU = "documentations"
        private const val BROADCAST = "broadcast" // chaosradio

        private const val EVENT_GROUP = "Events"
        private const val DOCU_GROUP = "Documentations"
        private const val ERFA_GROUP = "Erfas"
        private const val BROADCAST_GROUP = "Broadcast"

        private const val DEFAULT_CONFERENCE_GROUP = "other conferences"
        private const val MIN_NUM_CONS = 3 // minimum number of conferences to be listed as its own group
    }
}
