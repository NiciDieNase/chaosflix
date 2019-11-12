package de.nicidienase.chaosflix.common

import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import kotlin.collections.ArrayList

object ChaosflixUtil {
    fun getOptimalRecording(recordings: List<Recording>, originalLanguage: String = ""): Recording? {
        val result = getOrderedRecordings(recordings, originalLanguage)
        return when {
            result.size > 0 -> result[0]
            else -> null
        }
    }

    fun getRecordingForThumbs(recordings: List<Recording>): Recording? {
        val lqRecordings = recordings.filter { !it.isHighQuality && it.width > 0 }.sortedBy { it.size }
        return when {
        lqRecordings.size > 0 -> lqRecordings[0]
            else -> null
        }
    }

    private fun getOrderedRecordings(
        recordings: List<Recording>,
        originalLanguage: String
    ): ArrayList<Recording> {
        val result = ArrayList<Recording>()

        var hqRecordings = recordings
            .filter { it.isHighQuality && it.mimeType == "video/mp4" }
            .sortedBy { it.language.length }
        var lqRecordings = recordings
            .filter { !it.isHighQuality && it.mimeType == "video/mp4" }
            .sortedBy { it.language.length }

        if (originalLanguage.isNotBlank()) {
            hqRecordings = hqRecordings.filter { it.language == originalLanguage }
            lqRecordings = lqRecordings.filter { it.language == originalLanguage }
        }
        result.addAll(hqRecordings)
        result.addAll(lqRecordings)
        return result
    }

    fun getStringForTag(tag: String): String {
        when (tag) {
            "broadcast/chaosradio" -> return "Chaosradio"
            "blinkenlights" -> return "Blinkenlights"
            "camp" -> return "Camp"
            "chaoscologne" -> return "1c2 Chaos Cologne"
            "cryptocon" -> return "CryptoCon"
            "congress" -> return "Congress"
            "denog" -> return "DENOG"
            "datenspuren" -> return "Datenspuren"
            "easterhegg" -> return "Easterhegg"
            "fiffkon" -> return "FifFKon"
            "froscon" -> return "FrOSCon"
            "gpn" -> return "GPN"
            "hackover" -> return "Hackover"
            "mrmcd" -> return "MRMCD"
            "netzpolitik" -> return "Das ist Netzpolitik!"
            "sendezentrum" -> return "Sendezentrum"
            "sigint" -> return "SIGINT"
            "vcfb" -> return "Vintage Computing Festival Berlin"

            "other conferences" -> return "Other Conferences"
            else -> return tag
        }
    }

    val orderedConferencesList: List<String> = listOf(
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
            "cryptocon")
}