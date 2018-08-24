package de.nicidienase.chaosflix.common.entities.recording.persistence

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

@Dao
interface RelatedEventDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insertEvent(vararg events: PersistentRelatedEvent): LongArray

	@Update
	fun updateEvent(vararg events: PersistentRelatedEvent)

	@Delete
	fun deleteEvent(vararg event: PersistentRelatedEvent)

	@Query("SELECT * FROM related WHERE eventId = :id")
	fun getRelatedEventsForEvent(id: Long): LiveData<List<PersistentRelatedEvent>>

}
