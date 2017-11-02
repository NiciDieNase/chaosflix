package de.nicidienase.chaosflix.common.entities.recording.persistence

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Flowable

@Dao
interface ConferenceDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertConferences(vararg conferences: PersistentConference): LongArray

    @Query("SELECT * FROM conference")
    fun getAllConferences(): Flowable<List<PersistentConference>>

    @Query("SELECT * FROM conference WHERE title LIKE :search")
    fun findConferenceByTitle(search: String): Flowable<List<PersistentConference>>

    @Query("SELECT * FROM conference WHERE conferenceId = :id LIMIT 1")
    fun findConferenceById(id: Long): Flowable<PersistentConference>

    @Query("SELECT * FROM conference WHERE conferenceGroupId = :id")
    fun findConferenceByGroup(id: Long): Flowable<List<PersistentConference>>
}
