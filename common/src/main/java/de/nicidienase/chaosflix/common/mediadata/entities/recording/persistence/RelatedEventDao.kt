package de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query

@Dao
abstract class RelatedEventDao : BaseDao<PersistentRelatedEvent>() {
	@Query("SELECT * FROM related WHERE parentEventId = :id")
	abstract fun getRelatedEventsForEvent(id: Long): LiveData<List<PersistentRelatedEvent>>

	@Query("SELECT * FROM related WHERE parentEventId = :id")
	abstract fun getRelatedEventsForEventSync(id: Long): List<PersistentRelatedEvent>

	@Query("SELECT * FROM related WHERE parentEventId = :parentId AND relatedEventGuid = :related")
	abstract fun findSpecificRelatedEventSync(parentId: Long, related: String): PersistentRelatedEvent?

	@Query("DElETE FROM related")
	abstract fun delete()

	override fun updateOrInsertInternal(item: PersistentRelatedEvent) {
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
