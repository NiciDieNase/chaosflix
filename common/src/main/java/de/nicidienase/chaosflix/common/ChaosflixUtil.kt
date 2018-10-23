package de.nicidienase.chaosflix.common

import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentRecording
import java.util.*
import kotlin.collections.ArrayList

object ChaosflixUtil {
    fun getOptimalRecording(recordings: List<PersistentRecording>): PersistentRecording? {
        val result = ArrayList<PersistentRecording>()

        result.addAll(recordings.filter { it.isHighQuality && it.mimeType == "video/mp4" }.sortedBy { it.language.length })
        result.addAll(recordings.filter { !it.isHighQuality && it.mimeType == "video/mp4" }.sortedBy { it.language.length })

        when {
            result.size > 0 -> return result[0]
            else -> return null
        }
    }

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

    val orderedConferencesList: List<String> = Arrays.asList("congress",
            "gpn",
            "mrmcd",
            "broadcast/chaosradio",
            "eh",
            "camp",
            "froscon",
            "sendezentrum",
            "sigint",
            "datenspuren",
            "fiffkon",
            "cryptocon")
}