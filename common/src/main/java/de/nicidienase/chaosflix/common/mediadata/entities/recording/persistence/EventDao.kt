package de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

@Dao
interface EventDao: PersistentItemDao<PersistentEvent> {

    @Query("SELECT * FROM event")
    fun getAllEvents(): LiveData<List<PersistentEvent>>

    @Query("SELECT * FROM event")
    fun getAllEventsSync(): List<PersistentEvent>

    @Query("SELECT * FROM event WHERE title LIKE :search ORDER BY title ASC")
    fun findEventByTitle(search: String): LiveData<PersistentEvent>

    @Query("SELECT * FROM event WHERE id = :id ORDER BY title ASC")
    fun findEventById(id: Long): LiveData<PersistentEvent>

    @Query("SELECT * FROM event WHERE id in (:ids)")
    fun findEventsByIds(ids: LongArray): LiveData<List<PersistentEvent>>

    @Query("SELECT * FROM event WHERE guid in (:ids)")
    fun findEventsByGUIDs(ids: List<String>): LiveData<List<PersistentEvent>>

    @Query("SELECT * FROM event WHERE conference = :id ORDER BY title ASC")
    fun findEventsByConference(id: Long):LiveData<List<PersistentEvent>>

    @Query("SELECT * FROM event WHERE conference = :id ORDER BY title ASC")
    fun findEventsByConferenceSync(id: Long):List<PersistentEvent>

    @Query("SELECT * FROM event INNER JOIN watchlist_item WHERE event.id = watchlist_item.event_id")
    fun findBookmarkedEvents(): LiveData<List<PersistentEvent>>

    @Query("SELECT * FROM event INNER JOIN playback_progress WHERE event.id = playback_progress.event_id")
    fun findInProgressEvents(): LiveData<List<PersistentEvent>>

    @Query("SELECT * FROM event WHERE frontendLink = :url ")
    fun findEventsByFrontendurl(url: String):LiveData<PersistentEvent>

    @Query("DELETE FROM event WHERE id = :conferenceId")
    fun deleteEventsForConference(conferenceId: Long)
}