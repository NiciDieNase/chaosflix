package de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
abstract class ConferenceGroupDao : BaseDao<ConferenceGroup>() {
    @Query("SELECT * FROM conference_group ORDER BY order_index")
    abstract fun getAll(): LiveData<List<ConferenceGroup>>

    @Query("SELECT * FROM conference_group WHERE name = :name LIMIT 1")
    abstract suspend fun getConferenceGroupByName(name: String): ConferenceGroup?

    @Query("DELETE FROM conference_group WHERE id NOT IN (SELECT conference.conferenceGroupId FROM conference)")
    abstract fun deleteEmptyGroups()

    @Query("DElETE FROM conference_group")
    abstract fun delete()

    override suspend fun updateOrInsertInternal(item: ConferenceGroup): Long {
        if (item.id != 0L) {
            update(item)
        } else {
            val existingGroup = getConferenceGroupByName(item.name)
            if (existingGroup != null) {
                item.id = existingGroup.id
                update(item)
            } else {
                item.id = insert(item)
            }
        }
        return item.id
    }
}
