package de.nicidienase.chaosflix.common

import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import kotlin.collections.ArrayList

object ChaosflixUtil {
    fun getOptimalRecording(recordings: List<Recording>, originalLanguage: String): Recording {
        val groupedRecordings =
            recordings.groupBy { "${if (it.isHighQuality) "HD" else "SD"}-${it.mimeType}" }
        return when {
            groupedRecordings.keys.contains(HD_MP4) ->
                getRecordingForGroup(groupedRecordings[HD_MP4], originalLanguage)
            groupedRecordings.keys.contains(HD_WEBM) ->
                getRecordingForGroup(groupedRecordings[HD_WEBM], originalLanguage)
            groupedRecordings.keys.contains(SD_MP4) ->
                getRecordingForGroup(groupedRecordings[SD_MP4], originalLanguage)
            groupedRecordings.keys.contains(SD_WEBM) ->
                getRecordingForGroup(groupedRecordings[SD_WEBM], originalLanguage)
            else -> recordings.first()
        }
    }

    fun getRecordingForThumbs(recordings: List<Recording>): Recording? {
        val lqRecordings = recordings.filter { !it.isHighQuality && it.width > 0 }.sortedBy { it.size }
        return when {
            lqRecordings.isNotEmpty() -> lqRecordings[0]
            else -> null
        }
    }

    private fun getRecordingForGroup(group: List<Recording>?, language: String): Recording {
        if (group.isNullOrEmpty()) {
            error("Got empty or null list, this should not happen!")
        }
        return when {
            group.size == 1 -> group.first()
            else -> {
                val languageFiltered = group.filter { it.language == language }
                val relaxedLanguageFiltered = group.filter { it.language.contains(language) }
                when {
                    languageFiltered.isNotEmpty() -> languageFiltered.first()
                    relaxedLanguageFiltered.isNotEmpty() -> relaxedLanguageFiltered.first()
                    else -> group.first()
                }
            }
        }
    }

    private fun getOrderedRecordings(
        recordings: List<Recording>,
        originalLanguage: String
    ): ArrayList<Recording> {
        val result = ArrayList<Recording>()

        var hqMp4Recordings = recordings
            .filter { it.isHighQuality && it.mimeType == "video/mp4" }
            .sortedBy { it.language.length }
        var lqMp4Recordings = recordings
            .filter { !it.isHighQuality && it.mimeType == "video/mp4" }
            .sortedBy { it.language.length }

        if (originalLanguage.isNotBlank()) {
            hqMp4Recordings = hqMp4Recordings.filter { it.language == originalLanguage }
            lqMp4Recordings = lqMp4Recordings.filter { it.language == originalLanguage }
        }
        result.addAll(hqMp4Recordings)
        result.addAll(lqMp4Recordings)
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

    private const val HD_MP4 = "HD-video/mp4"
    private const val HD_WEBM = "HD-video/webm"
    private const val SD_MP4 = "SD-video/mp4"
    private const val SD_WEBM = "SD-video/webm"
}
