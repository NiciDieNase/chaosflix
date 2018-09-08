package de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

@Dao
abstract class ConferenceDao: BaseDao<PersistentConference>() {

    @Query("SELECT * FROM conference")
    abstract fun getAllConferences(): LiveData<List<PersistentConference>>

    @Query("SELECT * FROM conference WHERE title LIKE :search")
    abstract fun findConferenceByTitle(search: String): LiveData<List<PersistentConference>>

    @Query("SELECT * FROM conference WHERE id = :id LIMIT 1")
    abstract fun findConferenceById(id: Long): LiveData<PersistentConference>

    @Query("SELECT * FROM conference WHERE acronym = :acronym LIMIT 1")
    abstract fun findConferenceByAcronymSync(acronym: String): PersistentConference


    @Query("SELECT * FROM conference WHERE conferenceGroupId = :id ORDER BY acronym DESC")
    abstract fun findConferenceByGroup(id: Long): LiveData<List<PersistentConference>>

}
