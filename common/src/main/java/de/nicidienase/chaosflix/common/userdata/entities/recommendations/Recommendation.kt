package de.nicidienase.chaosflix.common.userdata.entities.recommendations

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "recommendation",
        indices = [Index(value = ["event_guid", "channel"], unique = true)])
data class Recommendation(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    @ColumnInfo(name = "event_guid")
    var eventGuid: String,
    var channel: String,
    @ColumnInfo(name = "programm_id")
    var programmId: Long,
    var dismissed: Boolean = false
)
