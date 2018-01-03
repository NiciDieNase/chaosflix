package de.nicidienase.chaosflix.common.entities.recording.persistence

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

@Dao
interface ConferenceDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertConferences(vararg conferences: PersistentConference): LongArray

    @Query("SELECT * FROM conference")
    fun getAllConferences(): LiveData<List<PersistentConference>>

    @Query("SELECT * FROM conference WHERE title LIKE :search")
    fun findConferenceByTitle(search: String): LiveData<List<PersistentConference>>

    @Query("SELECT * FROM conference WHERE conferenceId = :id LIMIT 1")
    fun findConferenceById(id: Long): LiveData<PersistentConference>

    @Query("SELECT * FROM conference WHERE conferenceGroupId = :id ORDER BY acronym DESC")
    fun findConferenceByGroup(id: Long): LiveData<List<PersistentConference>>

    @Delete
    fun deleteConference(vararg conference: PersistentConference)
}
