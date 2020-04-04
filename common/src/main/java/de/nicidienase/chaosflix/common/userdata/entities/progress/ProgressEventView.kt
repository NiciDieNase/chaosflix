package de.nicidienase.chaosflix.common.userdata.entities.progress

import androidx.room.ColumnInfo
import androidx.room.Embedded
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event

class ProgressEventView(
    val progress: Long,
    @ColumnInfo(name = "watch_date")
    val watchDate: Long,
    @Embedded
    val event: Event
)
