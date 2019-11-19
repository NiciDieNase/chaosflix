package de.nicidienase.chaosflix.common.userdata.entities.download

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface OfflineEventDao {

    @Insert
    fun insert(vararg items: OfflineEvent)

    @Query("SELECT * FROM offline_event WHERE event_guid = :guid LIMIT 1")
    fun getByEventGuid(guid: String): LiveData<OfflineEvent?>

    @Query("SELECT * FROM offline_event WHERE event_guid = :guid LIMIT 1")
    fun getByEventGuidSync(guid: String): OfflineEvent?

    @Query("SELECT * FROM offline_event WHERE download_reference = :ref LIMIT 1")
    fun getByDownloadReference(ref: Long): LiveData<OfflineEvent?>

    @Query("SELECT * FROM offline_event WHERE download_reference = :ref LIMIT 1")
    fun getByDownloadReferenceSync(ref: Long): OfflineEvent?

    @Query("SELECT * FROM offline_event")
    fun getAll(): LiveData<List<OfflineEvent>>

    @Query("SELECT * FROM offline_event")
    fun getAllSync(): List<OfflineEvent>

    @Query("DELETE FROM offline_event WHERE id=:id")
    fun deleteById(id: Long)

    @Query("SELECT o.event_guid,o.recording_id,o.download_reference,o.local_path,e.title,e.subtitle,e.length,e.thumbUrl FROM offline_event o JOIN event e WHERE o.event_guid = e.guid")
    fun getOfflineEventsDisplay(): LiveData<List<OfflineEventView>>
}