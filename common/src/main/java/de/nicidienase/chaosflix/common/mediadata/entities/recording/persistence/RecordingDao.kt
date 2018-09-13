package de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query

@Dao
abstract class RecordingDao: BaseDao<PersistentRecording>() {

    @Query("SELECT * FROM recording")
    abstract fun getAllRecordings(): LiveData<List<PersistentRecording>>

    @Query("SELECT * FROM recording WHERE id = :id LIMIT 1")
    abstract fun findRecordingById(id: Long): LiveData<PersistentRecording>

    @Query("SELECT * FROM recording WHERE eventId = :id")
    abstract fun findRecordingByEvent(id: Long): LiveData<List<PersistentRecording>>

    @Query("SELECT * FROM recording WHERE eventId = :id")
    abstract fun findRecordingByEventSync(id: Long): List<PersistentRecording>

    @Query("SELECT * FROM recording WHERE backendId = :backendId")
    abstract fun findRecordingByBackendIdSync(backendId: Long): PersistentRecording?

    @Query("DELETE FROM recording WHERE eventId = :eventId")
    abstract fun deleteRecordingsForEvent(eventId: Long)

    override fun updateOrInsertInternal(item: PersistentRecording) {
        if (!item.id.equals(0)) {
            update(item)
        } else {
            val existingRecording = getExistingItem(item)
            if (existingRecording != null) {
                item.id = existingRecording.id
                update(item)
            } else {
                item.id = insert(item)
            }
        }
    }

    private fun getExistingItem(item: PersistentRecording) = findRecordingByBackendIdSync(item.backendId)

}