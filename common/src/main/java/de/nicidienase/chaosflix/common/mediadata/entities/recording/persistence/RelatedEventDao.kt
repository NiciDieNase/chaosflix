package de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query

@Dao
abstract class RelatedEventDao: BaseDao<PersistentRelatedEvent>() {
	@Query("SELECT * FROM related WHERE parentEventId = :id")
	abstract fun getRelatedEventsForEvent(id: Long): LiveData<List<PersistentRelatedEvent>>

	@Query("SELECT * FROM related WHERE parentEventId = :id")
	abstract fun getRelatedEventsForEventSync(id: Long): List<PersistentRelatedEvent>
}
