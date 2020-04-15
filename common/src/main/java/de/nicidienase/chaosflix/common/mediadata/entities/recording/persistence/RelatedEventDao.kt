package de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
abstract class RelatedEventDao : BaseDao<RelatedEvent>() {
    @Query("SELECT * FROM related WHERE parentEventId = :id")
    abstract fun getRelatedEventsForEvent(id: Long): LiveData<List<RelatedEvent>>

    @Query("SELECT * FROM related WHERE parentEventId = :id")
    abstract suspend fun getRelatedEventsForEventSuspend(id: Long): List<RelatedEvent>

    @Query("""SELECT event.* FROM related JOIN event ON related.relatedEventGuid = event.guid WHERE related.parentEventId = :id ORDER BY related.weight DESC""")
    abstract fun newGetReletedEventsForEvent(id: Long): LiveData<List<Event>>

    @Query("SELECT * FROM related WHERE parentEventId = :parentId AND relatedEventGuid = :related")
    abstract suspend fun findSpecificRelatedEvent(parentId: Long, related: String): RelatedEvent?

    @Query("DElETE FROM related")
    abstract fun delete()

    override suspend fun updateOrInsertInternal(item: RelatedEvent): Long {
        if (item.id != 0L) {
            update(item)
        } else {
            val existingItem = findSpecificRelatedEvent(item.parentEventId, item.relatedEventGuid)
            if (existingItem != null) {
                item.id = existingItem.id
                update(item)
            } else {
                item.id = insert(item)
            }
        }
        return item.id
    }
}
