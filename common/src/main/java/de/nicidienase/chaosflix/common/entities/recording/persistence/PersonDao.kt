package de.nicidienase.chaosflix.common.entities.recording.persistence

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Flowable

@Dao
interface PersonDao{
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertPersion(vararg person: Person)

    @Query("SELECT * FROM person")
    fun getAll(): Flowable<List<Person>>

    @Query("SELECT * FROM person WHERE personID = :id")
    fun getByID(id: Long): Flowable<List<Person>>

    @Query("SELECT * FROM person WHERE name = :name")
    fun getByName(): Flowable<List<Person>>

    @Query("SELECT * FROM speaker_relation INNER JOIN person on speaker_relation.personID = person.personID WHERE speaker_relation.eventId = :id")
    fun getSpeakerForEvent(id: Long): Flowable<List<Person>>
}