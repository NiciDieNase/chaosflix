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

    @Query("SELECT * FROM event WHERE conferenceId = :id ORDER BY title ASC")
    fun findEventsByConference(id: Long):LiveData<List<PersistentEvent>>
}