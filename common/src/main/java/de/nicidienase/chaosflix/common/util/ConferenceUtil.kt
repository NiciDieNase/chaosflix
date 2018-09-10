package de.nicidienase.chaosflix.common.util

import java.util.*

object ConferenceUtil {
	@JvmStatic
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

	@JvmStatic
	val orderedConferencesList: List<String> = Arrays.asList(
			"congress", "sendezentrum", "camp",
			"gpn", "mrmcd", "broadcast/chaosradio",
			"eh", "froscon", "sigint",
			"datenspuren", "fiffkon", "cryptocon")
}