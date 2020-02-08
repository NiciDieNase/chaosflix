package de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence

import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Transaction
import android.arch.persistence.room.Update

abstract class BaseDao<in T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(item: T): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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
    open fun updateOrInsert(item: T) {
        updateOrInsertInternal(item)
    }

    @Transaction
    open fun updateOrInsert(vararg events: T) {
        events.map { updateOrInsertInternal(it) }
    }

    protected abstract fun updateOrInsertInternal(item: T)
}
