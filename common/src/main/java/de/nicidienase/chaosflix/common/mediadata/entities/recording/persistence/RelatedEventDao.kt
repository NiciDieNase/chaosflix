package de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
abstract class RelatedEventDao : BaseDao<RelatedEvent>() {
    @Query("SELECT * FROM related WHERE parentEventId = :id")
    abstract fun getRelatedEventsForEvent(id: Long): LiveData<List<RelatedEvent>>

    @Query("SELECT * FROM related WHERE parentEventId = :id")
    abstract fun getRelatedEventsForEventSync(id: Long): List<RelatedEvent>

    @Query("SELECT * FROM related WHERE parentEventId = :parentId AND relatedEventGuid = :related")
    abstract fun findSpecificRelatedEventSync(parentId: Long, related: String): RelatedEvent?

    @Query("DElETE FROM related")
    abstract fun delete()

    override fun updateOrInsertInternal(item: RelatedEvent) {
        if (item.id != 0L) {
            update(item)
        } else {
            val existingItem = findSpecificRelatedEventSync(item.parentEventId, item.relatedEventGuid)
            if (existingItem != null) {
                item.id = existingItem.id
                update(item)
            } else {
                item.id = insert(item)
            }
        }
    }
}
