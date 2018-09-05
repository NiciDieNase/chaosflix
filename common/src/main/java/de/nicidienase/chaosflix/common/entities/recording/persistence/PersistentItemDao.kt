package de.nicidienase.chaosflix.common.entities.recording.persistence

import android.arch.persistence.room.*

@Dao
interface PersistentItemDao<in PersistenItem> {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(item: PersistentItem): Long

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(vararg items: PersistentItem): LongArray

	@Update
	fun update(item: PersistentItem)

	@Update
	fun update(vararg items: PersistentItem)

	@Delete
	fun delete(item: PersistentItem)

	@Delete
	fun delete(vararg items: PersistentItem)
}