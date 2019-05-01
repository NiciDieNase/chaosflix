package de.nicidienase.chaosflix.touch.browse.streaming

import de.nicidienase.chaosflix.common.mediadata.entities.streaming.Group
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.LiveConference
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.Room

data class StreamingItem(
    val conference: LiveConference,
    val group: Group,
    val room: Room
)