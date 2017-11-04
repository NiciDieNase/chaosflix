package de.nicidienase.chaosflix.common.entities.recording.persistence

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface ConferenceGroupDao{
    @Query("SELECT * FROM conference_group ORDER BY order_index")
    fun getAll(): LiveData<List<ConferenceGroup>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addConferenceGroup(vararg conferenceGroup: ConferenceGroup): LongArray

    @Query("SELECT * FROM conference_group WHERE name = :name LIMIT 1")
    fun getConferenceGroupByName(name: String): ConferenceGroup?
}