package de.nicidienase.chaosflix.common.entities.recording.persistence

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface RecordingDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecording(vararg recordings: PersistentRecording): LongArray

    @Query("SELECT * FROM recording")
    fun getAllRecordings(): LiveData<List<PersistentRecording>>

    @Query("SELECT * FROM recording WHERE recordingId = :id LIMIT 1")
    fun findRecordingById(id: Long): LiveData<PersistentRecording>

    @Query("SELECT * FROM recording WHERE eventId = :id")
    fun findRecordingByEvent(id: Long): LiveData<List<PersistentRecording>>
}