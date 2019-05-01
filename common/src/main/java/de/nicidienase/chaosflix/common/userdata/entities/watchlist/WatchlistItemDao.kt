package de.nicidienase.chaosflix.common.userdata.entities.watchlist

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Delete

@Dao
interface WatchlistItemDao {
    @Query("SELECT * from watchlist_item")
    fun getAll(): LiveData<List<WatchlistItem>>

    @Query("SELECT * from watchlist_item")
    fun getAllSync(): List<WatchlistItem>

    @Query("SELECT * from watchlist_item WHERE event_guid = :guid LIMIT 1")
    fun getItemForEvent(guid: String): LiveData<WatchlistItem?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveItem(item: WatchlistItem)

    @Delete
    fun deleteItem(item: WatchlistItem)

    @Query("DELETE from watchlist_item WHERE event_guid = :guid")
    fun deleteItem(guid: String)
}