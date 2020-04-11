package de.nicidienase.chaosflix.common.userdata.entities.progress

import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Relation
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event

@Keep
class ProgressEventView(
    @Embedded
    val progress: PlaybackProgress,
    @Relation(entityColumn = "guid", parentColumn = "event_guid")
    var event: Event?
)
