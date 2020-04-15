package de.nicidienase.chaosflix.common.userdata.entities.recommendations

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.BaseDao

@Dao
abstract class RecommendationDao : BaseDao<Recommendation>() {

    @Transaction
    @Query("SELECT * FROM recommendation WHERE channel=:channel")
    abstract suspend fun getAllForChannel(channel: String): List<RecommendationEventView>

    override suspend fun updateOrInsertInternal(item: Recommendation): Long {
        return if (item.id != 0L) {
            update(item)
            item.id
        } else {
            insert(item)
        }
    }

    @Query("UPDATE recommendation SET dismissed = 1 WHERE programm_id = :programmId")
    abstract suspend fun markDismissed(programmId: Long)

    @Query("SELECT * FROM recommendation WHERE channel = :channel AND event_guid = :guid")
    abstract suspend fun getForChannelAndEvent(channel: String, guid: String): List<Recommendation>
}
