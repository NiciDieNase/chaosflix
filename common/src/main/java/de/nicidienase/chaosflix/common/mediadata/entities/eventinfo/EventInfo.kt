package de.nicidienase.chaosflix.common.mediadata.entities.eventinfo

import androidx.room.Entity
import de.nicidienase.chaosflix.common.mediadata.entities.eventinfo.dto.VocEventDto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity
data class EventInfo(
        val name: String,
        val location: String,
        val streaming: Boolean?,
        val startDate: Date,
        val endDate: Date
) {
    companion object {
        fun fromVocEventDto(dto: VocEventDto): EventInfo? {
            return if(dto.name == null
                    || dto.location == null
                    || dto.startDate == null
                    || dto.endDate == null
            ) {
                null
            } else {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val startDate = dateFormat.parse(dto.startDate)
                val endDate = dateFormat.parse(dto.endDate)
                EventInfo(dto.name, dto.location, dto.streaming, startDate, endDate)
            }
        }
    }
}

