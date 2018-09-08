package de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query

@Dao
abstract class ConferenceGroupDao: BaseDao<ConferenceGroup>() {
    @Query("SELECT * FROM conference_group ORDER BY order_index")
    abstract fun getAll(): LiveData<List<ConferenceGroup>>

    @Query("SELECT * FROM conference_group WHERE name = :name LIMIT 1")
    abstract fun getConferenceGroupByName(name: String): ConferenceGroup?
}