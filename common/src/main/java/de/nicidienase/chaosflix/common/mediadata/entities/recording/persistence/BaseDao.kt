package de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence

import android.arch.persistence.room.*

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
}