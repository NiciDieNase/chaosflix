package de.nicidienase.chaosflix.touch.browse.streaming

import de.nicidienase.chaosflix.common.entities.streaming.Group
import de.nicidienase.chaosflix.common.entities.streaming.LiveConference
import de.nicidienase.chaosflix.common.entities.streaming.Room

data class StreamingItem(val conference: LiveConference,
                         val group: Group,
                         val room: Room)