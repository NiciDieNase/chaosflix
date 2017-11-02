package de.nicidienase.chaosflix.common.entities.recording.persistence

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Flowable

@Dao
interface ConferenceGroupDao{
    @Query("SELECT * FROM conference_group")
    fun getAll(): Flowable<List<ConferenceGroup>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addConferenceGroup(vararg conferenceGroup: ConferenceGroup): LongArray

    @Query("SELECT * FROM conference_group WHERE name = :name LIMIT 1")
    fun getConferenceGroupByName(name: String): ConferenceGroup?
}