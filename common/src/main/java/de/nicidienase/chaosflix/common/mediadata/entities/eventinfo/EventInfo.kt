package de.nicidienase.chaosflix.common.mediadata.entities.eventinfo

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.nicidienase.chaosflix.common.mediadata.entities.eventinfo.dto.VocEventDto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
@Entity(tableName = "event_info")
data class EventInfo(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var name: String,
    var location: String,
    var streaming: Boolean?,
    var startDate: Date,
    var endDate: Date,
    var description: String?
) {
    fun getDateText(): String {
        val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return if (startDate == endDate) {
            simpleDateFormat.format(startDate)
        } else {
            "${simpleDateFormat.format(startDate)} - ${simpleDateFormat.format(endDate)}"
        }
    }

    companion object {
        fun fromVocEventDto(dto: VocEventDto): EventInfo? {
            return if (dto.name == null ||
                    dto.location == null ||
                    dto.startDate == null ||
                    dto.endDate == null
            ) {
                null
            } else {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val startDate = dateFormat.parse(dto.startDate)
                val endDate = dateFormat.parse(dto.endDate)
                EventInfo(0, dto.name, dto.location, dto.streaming, startDate, endDate, dto.description)
            }
        }
    }
}
