package de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query

@Dao
interface ConferenceGroupDao: PersistentItemDao<ConferenceGroup>{
    @Query("SELECT * FROM conference_group ORDER BY order_index")
    fun getAll(): LiveData<List<ConferenceGroup>>

    @Query("SELECT * FROM conference_group WHERE name = :name LIMIT 1")
    fun getConferenceGroupByName(name: String): ConferenceGroup?
}