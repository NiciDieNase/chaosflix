package de.nicidienase.chaosflix.common.entities.recording.persistence

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
    fun getAllEvents(): Flowable<PersistentEvent>

    @Query("SELECT * FROM event WHERE title LIKE :search")
    fun findEventByTitle(search: String): Flowable<PersistentEvent>

    @Query("SELECT * FROM event WHERE eventId = :id")
    fun findEventById(id: Long): Flowable<PersistentEvent>

    @Query("SELECT * FROM event WHERE conferenceId = :id")
    fun findEventsByConference(id: Long):Flowable<PersistentEvent>
}