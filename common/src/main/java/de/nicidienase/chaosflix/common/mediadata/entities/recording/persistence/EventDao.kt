package de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

@Dao
abstract class EventDao: BaseDao<PersistentEvent>() {

    @Query("SELECT * FROM event")
    abstract fun getAllEvents(): LiveData<List<PersistentEvent>>

    @Query("SELECT * FROM event")
    abstract fun getAllEventsSync(): List<PersistentEvent>

    @Query("SELECT * FROM event WHERE title LIKE :search ORDER BY title ASC")
    abstract fun findEventByTitle(search: String): LiveData<PersistentEvent?>

    @Query("SELECT * FROM event WHERE id = :id ORDER BY title ASC")
    abstract fun findEventById(id: Long): LiveData<PersistentEvent?>

    @Query("SELECT * FROM event WHERE guid = :guid LIMIT 1")
    abstract fun findEventByGuid(guid: String): LiveData<PersistentEvent?>

    @Query("SELECT * FROM event WHERE guid = :guid LIMIT 1")
    abstract fun findEventByGuidSync(guid: String): PersistentEvent?

    @Query("SELECT * FROM event WHERE id in (:ids)")
    abstract fun findEventsByIds(ids: LongArray): LiveData<List<PersistentEvent>>

    @Query("SELECT * FROM event WHERE guid in (:ids)")
    abstract fun findEventsByGUIDs(ids: List<String>): LiveData<List<PersistentEvent>>

    @Query("SELECT * FROM event WHERE guid in (:guids)")
    abstract fun findEventsByGUIDsSync(guids: List<String>): List<PersistentEvent>

    @Query("SELECT * FROM event WHERE conferenceId = :id ORDER BY title ASC")
    abstract fun findEventsByConference(id: Long):LiveData<List<PersistentEvent>>

    @Query("SELECT * FROM event WHERE conferenceId = :id ORDER BY title ASC")
    abstract fun findEventsByConferenceSync(id: Long):List<PersistentEvent>

    @Query("SELECT * FROM event INNER JOIN watchlist_item WHERE event.guid = watchlist_item.event_guid")
    abstract fun findBookmarkedEvents(): LiveData<List<PersistentEvent>>

    @Query("SELECT * FROM event INNER JOIN playback_progress WHERE event.guid = playback_progress.event_guid")
    abstract fun findInProgressEvents(): LiveData<List<PersistentEvent>>

    @Query("SELECT * FROM event WHERE frontendLink = :url ")
    abstract fun findEventsByFrontendurl(url: String):LiveData<PersistentEvent?>

    override fun updateOrInsertInternal(event: PersistentEvent) {
        if (!event.id.equals(0)) {
            update(event)
        } else {
            val existingEvent = getExistingItem(event)
            if (existingEvent != null) {
                event.id = existingEvent.id
                update(event)
            } else {
                event.id = insert(event)
            }
        }
    }

    private fun getExistingItem(event: PersistentEvent) = findEventByGuidSync(event.guid)
}