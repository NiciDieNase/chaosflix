package de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
abstract class ConferenceDao : BaseDao<Conference>() {

    @Query("SELECT * FROM conference")
    abstract fun getAllConferences(): LiveData<List<Conference>>

    @Query("SELECT * FROM conference WHERE title LIKE :search")
    abstract fun findConferenceByTitle(search: String): LiveData<List<Conference>>

    @Query("SELECT * FROM conference WHERE id = :id LIMIT 1")
    abstract fun findConferenceById(id: Long): LiveData<Conference>

    @Query("SELECT * FROM conference WHERE acronym = :acronym LIMIT 1")
    abstract fun findConferenceByAcronymSync(acronym: String): Conference?

    @Query("SELECT * FROM conference WHERE acronym = :acronym LIMIT 1")
    abstract suspend fun findConferenceByAcronymSuspend(acronym: String): Conference?

    @Query("SELECT * FROM conference WHERE conferenceGroupId = :id ORDER BY acronym DESC")
    abstract fun findConferenceByGroup(id: Long): LiveData<List<Conference>>

    @Query("DELETE FROM conference")
    abstract fun delete()

    override fun updateOrInsertInternal(item: Conference) {
        if (item.id != 0L) {
            update(item)
        } else {
            val existingEvent = findConferenceByAcronymSync(item.acronym)
            if (existingEvent != null) {
                item.id = existingEvent.id
                update(item)
            } else {
                item.id = insert(item)
            }
        }
    }
}
