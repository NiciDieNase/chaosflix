package de.nicidienase.chaosflix.common.util

import java.util.*

object ConferenceUtil {
    @JvmStatic
    fun getStringForTag(tag: String): String {
        return when (tag) {
            "broadcast/chaosradio" -> "Chaosradio"
            "chaoscologne" -> "1c2 Chaos Cologne"
            "cryptocon" -> "CryptoCon"
            "denog" -> "DENOG"
            "fiffkon" -> "FifFKon"
            "froscon" -> "FrOSCon"
            "gpn" -> "GPN"
            "mrmcd" -> "MRMCD"
            "netzpolitik" -> "Das ist Netzpolitik!"
            "osmocon" -> "OsmoCon"
            "osc" -> "Open SUSE Conference"
            "sigint" -> "SIGINT"
            "vcfb" -> "Vintage Computing Festival Berlin"
            "other conferences" -> "Other Conferences"
            else -> tag.capitalize()
        }
    }

    fun getSensibleTags(tags: Set<String>, acronym: String): Set<String> {
        return tags.filterNot { it.matches("\\d+".toRegex()) }.filterNot { it == acronym }.toSet()
    }

    @JvmStatic
    val orderedConferencesList: List<String> = Arrays.asList(
            "congress",
            "gpn",
            "mrmcd",
            "broadcast/chaosradio",
            "easterhegg",
            "camp",
            "froscon",
            "sendezentrum",
            "sigint",
            "datenspuren",
            "fiffkon",
            "cryptocon"
    )
}