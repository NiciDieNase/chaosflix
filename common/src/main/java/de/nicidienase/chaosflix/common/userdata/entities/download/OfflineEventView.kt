package de.nicidienase.chaosflix.common.userdata.entities.download

import androidx.annotation.Keep
import androidx.room.ColumnInfo

@Keep
data class OfflineEventView(
    @ColumnInfo(name = "event_guid") var eventGuid: String,
    @ColumnInfo(name = "recording_id") var recordingId: Long,
    @ColumnInfo(name = "download_reference") var downloadReference: Long,
    @ColumnInfo(name = "local_path") var localPath: String,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "subtitle") var subtitle: String?,
    @ColumnInfo(name = "length") val length: Long,
    @ColumnInfo(name = "thumbUrl") var thumbUrl: String,
    var status: Int,
    @ColumnInfo(name = "status_icon") var statusIcon: Int,
    @ColumnInfo(name = "current_bytes") var currentBytes: Int,
    @ColumnInfo(name = "total_bytes") var totalBytes: Int
)
