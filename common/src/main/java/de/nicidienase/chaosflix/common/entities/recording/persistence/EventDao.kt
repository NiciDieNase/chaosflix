package de.nicidienase.chaosflix.common.entities.recording.persistence

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import de.nicidienase.chaosflix.common.entities.recording.Conference
import io.reactivex.Flowable

@Dao
interface EventDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEvent(vararg events: PersistentEvent): LongArray

    @Query("SELECT * FROM event")
    fun getAllEvents(): LiveData<PersistentEvent>

    @Query("SELECT * FROM event WHERE title LIKE :search ORDER BY title ASC")
    fun findEventByTitle(search: String): LiveData<PersistentEvent>

    @Query("SELECT * FROM event WHERE eventId = :id ORDER BY title ASC")
    fun findEventById(id: Long): LiveData<PersistentEvent>

    @Query("SELECT * FROM event WHERE conferenceId = :id ORDER BY title ASC")
    fun findEventsByConference(id: Long):LiveData<List<PersistentEvent>>
}