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


	override fun updateOrInsertInternal(item: PersistentRelatedEvent) {
		if (!item.id.equals(0)) {
			update(item)
		} else {
			val existingItem = getExistingItem(item)
			if (existingItem != null) {
				item.id = existingItem.id
				update(item)
			} else {
				item.id = insert(item)
			}
		}
	}

	private fun getExistingItem(item: PersistentRelatedEvent) = findSpecificRelatedEventSync(item.parentEventId, item.relatedEventGuid)


}
