package de.nicidienase.chaosflix.common.entities.download

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

@Dao
interface OfflineEventDao{

    @Insert
    fun insert(vararg items: OfflineEvent)

    @Query("SELECT * FROM offline_event WHERE event_id = :id LIMIT 1")
    fun getByEventId(id: Long): LiveData<OfflineEvent>

    @Query("SELECT * FROM offline_event WHERE event_id = :id LIMIT 1")
    fun getByEventIdSynchronous(id: Long): OfflineEvent

    @Query("SELECT * FROM offline_event WHERE download_reference = :ref LIMIT 1")
    fun getByDownloadReference(ref: Long): LiveData<OfflineEvent>

    @Query("SELECT * FROM offline_event WHERE download_reference = :ref LIMIT 1")
    fun getByDownloadReferenceSyncrounous(ref: Long): OfflineEvent

    @Query("SELECT * FROM offline_event")
    fun getAll(): LiveData<List<OfflineEvent>>

    @Query("SELECT * FROM offline_event")
    fun getAllSynchronous(): List<OfflineEvent>


    @Query("DELETE FROM offline_event WHERE id=:id")
    fun deleteById(id: Long)
}