package de.nicidienase.chaosflix.common.mediadata.entities.eventinfo

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.BaseDao

@Dao
abstract class EventInfoDao : BaseDao<EventInfo>() {

    @Query("SELECT * FROM event_info")
    abstract fun getAll(): LiveData<List<EventInfo>>

    @Query("SELECT * FROM event_info WHERE name LIKE :name")
    abstract fun findEventInfoByName(name: String): EventInfo?

    @Query("SELECT * FROM event_info WHERE startDate > :date AND streaming > 0 ORDER BY startDate ASC")
    abstract fun findEventWithStartDateAfter(date: Long): LiveData<List<EventInfo>>

    override suspend fun updateOrInsertInternal(item: EventInfo): Long {
        if (item.id != 0L) {
            update(item)
        } else {
            val existingEvent = findEventInfoByName(item.name)
            if (existingEvent != null) {
                item.id = existingEvent.id
                update(item)
            } else {
                item.id = insert(item)
            }
        }
        return item.id
    }
}
