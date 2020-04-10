package de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import androidx.room.Update

abstract class BaseDao<in T> {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun insert(item: T): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun insert(vararg items: T): LongArray

    @Update
    abstract fun update(item: T)

    @Update
    abstract fun update(vararg items: T)

    @Delete
    abstract fun delete(item: T)

    @Delete
    abstract fun delete(vararg items: T)

    @Transaction
    open suspend fun updateOrInsert(item: T): Long {
        return updateOrInsertInternal(item)
    }

    @Transaction
    open suspend fun updateOrInsert(vararg events: T) {
        events.map { updateOrInsertInternal(it) }
    }

    protected abstract suspend fun updateOrInsertInternal(item: T): Long
}
