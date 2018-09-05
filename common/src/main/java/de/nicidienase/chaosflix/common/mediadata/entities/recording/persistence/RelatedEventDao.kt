package de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import de.nicidienase.chaosflix.common.mediadata.entities.recording.RelatedEvent

@Dao
interface RelatedEventDao: PersistentItemDao<RelatedEvent> {
	@Query("SELECT * FROM related WHERE parentEventId = :id")
	fun getRelatedEventsForEvent(id: Long): LiveData<List<PersistentRelatedEvent>>
}
