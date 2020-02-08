package de.nicidienase.chaosflix.common.userdata.entities.download

import android.arch.persistence.room.ColumnInfo
import android.support.annotation.Keep

@Keep
data class OfflineEventView(
    @ColumnInfo(name = "event_guid") var eventGuid: String,
    @ColumnInfo(name = "recording_id") var recordingId: Long,
    @ColumnInfo(name = "download_reference") var downloadReference: Long,
    @ColumnInfo(name = "local_path") var localPath: String,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "subtitle") var subtitle: String?,
    @ColumnInfo(name = "length") val length: Long,
    @ColumnInfo(name = "thumbUrl") var thumbUrl: String
)
