package de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query

@Dao
abstract class ConferenceGroupDao : BaseDao<ConferenceGroup>() {
	@Query("SELECT * FROM conference_group ORDER BY order_index")
	abstract fun getAll(): LiveData<List<ConferenceGroup>>

	@Query("SELECT * FROM conference_group WHERE name = :name LIMIT 1")
	abstract fun getConferenceGroupByName(name: String): ConferenceGroup?

	override fun updateOrInsertInternal(group: ConferenceGroup) {
		if (!group.id.equals(0)) {
			update(group)
		} else {
			val existingGroup = getExistingItem(group)
			if (existingGroup != null) {
				group.id = existingGroup.id
				update(group)
			} else {
				group.id = insert(group)
			}
		}
	}

	private fun getExistingItem(group: ConferenceGroup) = getConferenceGroupByName(group.name)
}