package de.nicidienase.chaosflix.common.mediadata.entities.eventinfo.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class EventInfoWrapperDto(
        @SerializedName("voc_events") val events: Map<String, VocEventDto>,
        @SerializedName("voc_events_count") val eventsCount: CountInfo
)

data class CountInfo(
        val all: Int,
        val with_streaming: Int,
        val without_streaming: Int,
        val undefined_streaming: Int
)
