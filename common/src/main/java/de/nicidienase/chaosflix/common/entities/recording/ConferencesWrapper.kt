package de.nicidienase.chaosflix.common.entities.recording

import java.util.*
import kotlin.collections.HashMap


data class ConferencesWrapper(var conferences: List<Conference>) {
    private val CONGRESS = "congress"
    private val EVENTS = "events"
    private val CONFERENCES = "conferences"
    private val DEFAULT_CONFERENCE_GROUP = "other conferences"
    private val EVENT_GROUP = "Events"
    private val MIN_NUM_CONS = 1

    val conferencesMap: Map<String, List<Conference>>
        get() = generateConferencesMap()

    private fun generateConferencesMap(): HashMap<String, MutableList<Conference>> {
        val map = HashMap<String,MutableList<Conference>>()
        for (conference in conferences) {
            val split = conference.slug.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            when (split[0]) {
                CONGRESS -> if (split[1].endsWith("sendezentrum")) {
                    getListForTag(map,"sendezentrum").add(conference)

                } else {
                    getListForTag(map,CONGRESS).add(conference)
                }
                CONFERENCES -> when (split.size) {
                    2 -> if (split[1].startsWith("camp")) {
                        getListForTag(map,"camp").add(conference)
                    } else if (split[1].startsWith("sigint")) {
                        getListForTag(map,"sigint").add(conference)
                    } else if (split[1].startsWith("eh")) {
                        getListForTag(map,"eh").add(conference)
                    } else {
                        getListForTag(map,DEFAULT_CONFERENCE_GROUP).add(conference)
                    }
                    3 -> getListForTag(map,split[1]).add(conference)
                    else -> getListForTag(map,DEFAULT_CONFERENCE_GROUP).add(conference)
                }
                EVENTS -> getListForTag(map,EVENT_GROUP).add(conference)
                else -> getListForTag(map,conference.slug).add(conference)
            }
        }
        val other = map[DEFAULT_CONFERENCE_GROUP]
        val keySet = map.keys
        val removeList = ArrayList<String>()
        for (tag in keySet) {
            if (tag != DEFAULT_CONFERENCE_GROUP) {
                val list = map[tag]
                Collections.sort(list)
                Collections.reverse(list)
                if (list!!.size <= MIN_NUM_CONS) {
                    other?.addAll(list)
                    removeList.add(tag)
                }
            }
        }
        for (key in removeList) {
            map.remove(key)
        }
        return map
    }

    private fun getListForTag(map: MutableMap<String,MutableList<Conference>>, s: String): MutableList<Conference> {
        if (map.keys.contains(s)) {
            return map[s]!!
        } else {
            val list = ArrayList<Conference>()
            map.put(s, list)
            return list
        }
    }

    companion object {
        fun getStringForTag(tag: String): String {
            when (tag) {
                "congress" -> return "Congress"
                "sendezentrum" -> return "Sendezentrum"
                "camp" -> return "Camp"
                "broadcast/chaosradio" -> return "Chaosradio"
                "eh" -> return "Easterhegg"
                "gpn" -> return "GPN"
                "froscon" -> return "FrOSCon"
                "mrmcd" -> return "MRMCD"
                "sigint" -> return "SIGINT"
                "datenspuren" -> return "Datenspuren"
                "fiffkon" -> return "FifFKon"
                "blinkenlights" -> return "Blinkenlights"
                "chaoscologne" -> return "1c2 Chaos Cologne"
                "cryptocon" -> return "CryptoCon"
                "other conferences" -> return "Other Conferences"
                "denog" -> return "DENOG"
                "vcfb" -> return "Vintage Computing Festival Berlin"
                "hackover" -> return "Hackover"
                "netzpolitik" -> return "Das ist Netzpolitik!"
                else -> return tag
            }
        }

        val orderedConferencesList: List<String> = Arrays.asList(
                "congress", "sendezentrum", "camp",
                "gpn", "mrmcd", "broadcast/chaosradio",
                "eh", "froscon", "sigint",
                "datenspuren", "fiffkon", "cryptocon")
    }
}
