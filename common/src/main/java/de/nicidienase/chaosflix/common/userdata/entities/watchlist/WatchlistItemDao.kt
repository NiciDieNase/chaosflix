package de.nicidienase.chaosflix.common.userdata.entities.watchlist

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*


@Dao
interface WatchlistItemDao {
    @Query("SELECT * from watchlist_item")
    fun getAll(): LiveData<List<WatchlistItem>>

    @Query("SELECT * from watchlist_item WHERE event_id = :id LIMIT 1")
    fun getItemForEvent(id: Long): LiveData<WatchlistItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveItem(item: WatchlistItem)

    @Delete
    fun deleteItem(item: WatchlistItem)

    @Query("DELETE from watchlist_item WHERE event_id = :id")
    fun deleteItem(id: Long)
}