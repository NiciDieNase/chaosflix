package de.nicidienase.chaosflix.common.entities.userdata

import android.arch.persistence.room.*
import io.reactivex.Flowable


@Dao
interface WatchlistItemDao {
    @Query("SELECT * from watchlist_item")
    fun getAll(): Flowable<List<WatchlistItem>>

    @Query("SELECT * from watchlist_item WHERE id = :id LIMIT 1")
    fun getItemForEvent(id: Long): Flowable<WatchlistItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveItem(item: WatchlistItem)

    @Delete
    fun deleteItem(item: WatchlistItem)

    @Query("DELETE from watchlist_item WHERE id = :id")
    fun deleteItem(id: Long)
}