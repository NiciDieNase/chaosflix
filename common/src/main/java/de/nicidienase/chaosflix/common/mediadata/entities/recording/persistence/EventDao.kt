package de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
abstract class EventDao : BaseDao<Event>() {

    @Query("SELECT * FROM event")
    abstract fun getAllEvents(): LiveData<List<Event>>

    @Query("SELECT * FROM event")
    abstract fun getAllEventsSync(): List<Event>

    @Query("SELECT * FROM event WHERE title LIKE :search ORDER BY title ASC")
    abstract fun findEventByTitle(search: String): LiveData<Event?>

    @Query("SELECT * FROM event WHERE id = :id ORDER BY title ASC")
    abstract fun findEventById(id: Long): LiveData<Event?>

    @Query("SELECT * FROM event WHERE guid = :guid LIMIT 1")
    abstract fun findEventByGuid(guid: String): LiveData<Event?>

    @Query("""SELECT event.*, conference.acronym as conference FROM event 
        JOIN conference ON event.conferenceId = conference.id 
        WHERE guid = :guid LIMIT 1""")
    abstract suspend fun findEventByGuidSync(guid: String): Event?

    @Query("SELECT * FROM event WHERE id in (:ids)")
    abstract fun findEventsByIds(ids: LongArray): LiveData<List<Event>>

    @Query("SELECT * FROM event WHERE guid in (:ids)")
    abstract fun findEventsByGUIDs(ids: List<String>): LiveData<List<Event>>

    @Query("SELECT * FROM event WHERE guid in (:guids)")
    abstract suspend fun findEventsByGUIDsSuspend(guids: List<String>): List<Event>

    @Query("SELECT * FROM event WHERE isPromoted IS 1")
    abstract fun findPromotedEvents(): LiveData<List<Event>>

    @Query("SELECT * FROM event WHERE conferenceId = :id ORDER BY title ASC")
    abstract fun findEventsByConference(id: Long): LiveData<List<Event>>

    @Query("SELECT * FROM event WHERE conferenceId = :id ORDER BY title ASC")
    abstract fun findEventsByConferenceSync(id: Long): List<Event>

    @Query("SELECT event.* FROM event INNER JOIN watchlist_item WHERE event.guid = watchlist_item.event_guid")
    abstract fun findBookmarkedEvents(): LiveData<List<Event>>

    @Query("SELECT event.* FROM event INNER JOIN playback_progress WHERE event.guid = playback_progress.event_guid")
    abstract fun findInProgressEvents(): LiveData<List<Event>>

    @Query("SELECT * FROM event WHERE frontendLink LIKE :url ")
    abstract fun findEventsByFrontendurl(url: String): LiveData<List<Event?>>

    @Query("SELECT * FROM event WHERE frontendLink LIKE :url LIMIT 1")
    abstract suspend fun findEventForFrontendUrl(url: String): Event?

    @Query("DElETE FROM event")
    abstract fun delete()

    @Query("SELECT * FROM event WHERE link = :url LIMIT 1")
    abstract suspend fun findEventByFahrplanUrl(url: String): Event?

    @Query("SELECT * FROM event WHERE title LIKE :search LIMIT 1")
    abstract suspend fun findEventByTitleSuspend(search: String): Event?

    @Query("SELECT * FROM event WHERE title LIKE :title LIMIT 1")
    abstract fun findSingleEventByTitle(title: String): LiveData<Event?>

// 	@Query("SELECT * FROM event JOIN conference ON event.conferenceId=conference.id")
// 	abstract suspend fun getEventWithConference(eventId: Long): List<EventWithConference>
//
// 	@Query("SELECT * FROM event JOIN conference ON event.conferenceId=conference.id WHERE event.id = :eventId")
// 	abstract suspend fun getAllEventsWithConference(eventId: Long): List<EventWithConference>

    @Query("""SELECT event.*, conference.acronym as conference
    FROM event JOIN conference ON event.conferenceId=conference.id 
    WHERE conference.id = :confernceId
    ORDER BY event.title""")
    abstract fun getEventsWithConferenceForConfernce(confernceId: Long): LiveData<List<Event>>

    override suspend fun updateOrInsertInternal(item: Event): Long {
        if (item.id != 0L) {
            update(item)
        } else {
            val existingEvent = findEventByGuidSync(item.guid)
            if (existingEvent != null) {
                item.id = existingEvent.id
                update(item)
            } else {
                item.id = insert(item)
            }
        }
        return item.id
    }
}
