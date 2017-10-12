package de.nicidienase.chaosflix.common.entities

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import io.reactivex.Flowable
import io.reactivex.Observable

/**
 * Created by felix on 04.10.17.
 */

@Dao
interface WatchlistItemDao {
    @Query("SELECT * from watchlist_item")
    fun getAll(): Flowable<List<WatchlistItem>>

    @Query("SELECT * from watchlist_item WHERE id = :arg0 LIMIT 1")
    fun getItemForEvent(id:Int): Flowable<WatchlistItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveItem(item: WatchlistItem)

    @Delete
    fun deleteItem(item: WatchlistItem)
}