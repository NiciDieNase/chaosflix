package de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
abstract class PersistentItem(@PrimaryKey(autoGenerate = true)
                          var id: Long = 0)