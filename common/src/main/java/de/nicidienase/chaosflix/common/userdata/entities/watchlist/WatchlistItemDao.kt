package de.nicidienase.chaosflix.common.userdata.entities.watchlist

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.BaseDao

@Dao
abstract class WatchlistItemDao : BaseDao<WatchlistItem>() {

    @Query("SELECT * from watchlist_item")
    abstract fun getAll(): LiveData<List<WatchlistItem>>

    @Query("SELECT * from watchlist_item")
    abstract fun getAllSync(): List<WatchlistItem>

    @Query("SELECT * from watchlist_item WHERE event_guid = :guid LIMIT 1")
    abstract fun getItemForEvent(guid: String): LiveData<WatchlistItem?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveItem(item: WatchlistItem)

    @Delete
    abstract fun deleteItem(item: WatchlistItem)

    @Query("DELETE from watchlist_item WHERE event_guid = :guid")
    abstract fun deleteItem(guid: String)

    @Query("SELECT * from watchlist_item WHERE event_guid = :guid LIMIT 1")
    abstract suspend fun getItemForGuid(guid: String): WatchlistItem?

    override suspend fun updateOrInsertInternal(item: WatchlistItem) {
        if (item.id != 0L) {
            update(item)
        } else {
            val existingEvent = getItemForGuid(item.eventGuid)
            if (existingEvent != null) {
                item.id = existingEvent.id
                update(item)
            } else {
                item.id = insert(item)
            }
        }
    }
}
