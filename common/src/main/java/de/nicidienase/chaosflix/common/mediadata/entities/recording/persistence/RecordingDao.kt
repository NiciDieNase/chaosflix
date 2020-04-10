package de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
abstract class RecordingDao : BaseDao<Recording>() {

    @Query("SELECT * FROM recording")
    abstract fun getAllRecordings(): LiveData<List<Recording>>

    @Query("SELECT * FROM recording WHERE id = :id LIMIT 1")
    abstract fun findRecordingById(id: Long): LiveData<Recording>

    @Query("SELECT * FROM recording WHERE id = :id LIMIT 1")
    abstract fun findRecordingByIdSync(id: Long): Recording

    @Query("SELECT * FROM recording WHERE eventId = :id")
    abstract fun findRecordingByEvent(id: Long): LiveData<List<Recording>>

    @Query("SELECT * FROM recording WHERE eventId = :id")
    abstract suspend fun findRecordingByEventSync(id: Long): List<Recording>

    @Query("SELECT * FROM recording WHERE backendId = :backendId")
    abstract fun findRecordingByBackendId(backendId: Long): Recording?

    @Query("DELETE FROM recording WHERE eventId = :eventId")
    abstract fun deleteRecordingsForEvent(eventId: Long)

    @Query("DElETE FROM recording")
    abstract fun delete()

    override suspend fun updateOrInsertInternal(item: Recording): Long {
        if (item.id != 0L) {
            update(item)
        } else {
            val existingRecording = findRecordingByBackendId(item.backendId)
            if (existingRecording != null) {
                item.id = existingRecording.id
                update(item)
            } else {
                item.id = insert(item)
            }
        }
        return item.id
    }
}
