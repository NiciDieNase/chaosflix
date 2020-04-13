package de.nicidienase.chaosflix.common.userdata.entities.recommendations

import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Relation
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event

@Keep
class RecommendationEventView(
    @Embedded
    var recommendation: Recommendation,
    @Relation(entityColumn = "guid", parentColumn = "event_guid")
    var event: Event?
)
