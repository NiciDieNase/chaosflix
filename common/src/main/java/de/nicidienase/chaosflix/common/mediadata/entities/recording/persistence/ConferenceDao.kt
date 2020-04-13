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
    abstract suspend fun findConferenceByAcronym(acronym: String): Conference?

    @Query("SELECT * FROM conference WHERE acronym = :acronym LIMIT 1")
    abstract suspend fun findConferenceByAcronymSuspend(acronym: String): Conference?

    @Query("SELECT * FROM conference WHERE conferenceGroupId = :id ORDER BY acronym DESC")
    abstract fun findConferenceByGroup(id: Long): LiveData<List<Conference>>

    @Query("SELECT * FROM conference ORDER BY updatedAt DESC LIMIT :count")
    abstract fun getLatestConferences(count: Int): List<Conference>

    @Query("DELETE FROM conference")
    abstract fun delete()

    override suspend fun updateOrInsertInternal(item: Conference): Long {
        if (item.id != 0L) {
            update(item)
        } else {
            val existingEvent = findConferenceByAcronym(item.acronym)
            if (existingEvent != null) {
                item.id = existingEvent.id
                update(item)
            } else {
                item.id = insert(item)
            }
        }
        return item.id
    }
}
