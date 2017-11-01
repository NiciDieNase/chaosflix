package de.nicidienase.chaosflix.common.entities.recording.persistence

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Flowable

@Dao
interface RecordingDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecording(vararg recordings: PersistentRecording)

    @Query("SELECT * FROM recording")
    fun getAllRecordings(): Flowable<List<PersistentRecording>>

    @Query("SELECT * FROM recording WHERE recordingId = :id")
    fun findRecordingById(id: Long): Flowable<List<PersistentRecording>>
}