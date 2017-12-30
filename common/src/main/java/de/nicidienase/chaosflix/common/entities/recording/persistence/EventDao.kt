package de.nicidienase.chaosflix.common.entities.recording.persistence

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import de.nicidienase.chaosflix.common.entities.recording.Conference
import io.reactivex.Flowable

@Dao
interface EventDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEvent(vararg events: PersistentEvent): LongArray

    @Update
    fun updateEvent(vararg events: PersistentEvent)

    @Query("SELECT * FROM event")
    fun getAllEvents(): LiveData<PersistentEvent>

    @Query("SELECT * FROM event WHERE title LIKE :search ORDER BY title ASC")
    fun findEventByTitle(search: String): LiveData<PersistentEvent>

    @Query("SELECT * FROM event WHERE eventId = :id ORDER BY title ASC")
    fun findEventById(id: Long): LiveData<PersistentEvent>

    @Query("SELECT * FROM event WHERE eventId in (:ids)")
    fun findEventsByIds(ids: LongArray): LiveData<List<PersistentEvent>>

    @Query("SELECT * FROM event WHERE conferenceId = :id ORDER BY title ASC")
    fun findEventsByConference(id: Long):LiveData<List<PersistentEvent>>

    @Query("SELECT * FROM event INNER JOIN watchlist_item WHERE event.eventId = watchlist_item.event_id")
    fun findBookmarkedEvents(): LiveData<List<PersistentEvent>>

    @Query("SELECT * FROM event INNER JOIN playback_progress WHERE event.eventId = playback_progress.event_id")
    fun findInProgressEvents(): LiveData<List<PersistentEvent>>
}