package de.nicidienase.chaosflix.common.entities.userdata

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

/**
 * Created by felix on 04.10.17.
 */

@Dao
interface WatchlistItemDao {
    @Query("SELECT * from watchlist_item")
    fun getAll(): LiveData<List<WatchlistItem>>

    @Query("SELECT * from watchlist_item WHERE id = :id LIMIT 1")
    fun getItemForEvent(id:Int): LiveData<WatchlistItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveItem(item: WatchlistItem)

    @Delete
    fun deleteItem(item: WatchlistItem)
}