package de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query

@Dao
abstract class RecordingDao: BaseDao<Recording>() {

    @Query("SELECT * FROM recording")
    abstract fun getAllRecordings(): LiveData<List<Recording>>

    @Query("SELECT * FROM recording WHERE id = :id LIMIT 1")
    abstract fun findRecordingById(id: Long): LiveData<Recording>

    @Query("SELECT * FROM recording WHERE id = :id LIMIT 1")
    abstract fun findRecordingByIdSync(id: Long): Recording

    @Query("SELECT * FROM recording WHERE eventId = :id")
    abstract fun findRecordingByEvent(id: Long): LiveData<List<Recording>>

    @Query("SELECT * FROM recording WHERE eventId = :id")
    abstract fun findRecordingByEventSync(id: Long): List<Recording>

    @Query("SELECT * FROM recording WHERE backendId = :backendId")
    abstract fun findRecordingByBackendIdSync(backendId: Long): Recording?

    @Query("DELETE FROM recording WHERE eventId = :eventId")
    abstract fun deleteRecordingsForEvent(eventId: Long)

    @Query("DElETE FROM recording")
    abstract fun delete()

    override fun updateOrInsertInternal(item: Recording) {
        if (item.id != 0L) {
            update(item)
        } else {
            val existingRecording = findRecordingByBackendIdSync(item.backendId)
            if (existingRecording != null) {
                item.id = existingRecording.id
                update(item)
            } else {
                item.id = insert(item)
            }
        }
    }
}