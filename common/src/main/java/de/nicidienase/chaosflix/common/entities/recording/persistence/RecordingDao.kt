package de.nicidienase.chaosflix.common.entities.recording.persistence

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface RecordingDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecording(vararg recordings: PersistentRecording): LongArray

    @Query("SELECT * FROM recording")
    fun getAllRecordings(): Flowable<List<PersistentRecording>>

    @Query("SELECT * FROM recording WHERE recordingId = :id LIMIT 1")
    fun findRecordingById(id: Long): Flowable<PersistentRecording>

    @Query("SELECT * FROM recording WHERE eventId = :id")
    fun findRecordingByEvent(id: Long): Single<List<PersistentRecording>>
}