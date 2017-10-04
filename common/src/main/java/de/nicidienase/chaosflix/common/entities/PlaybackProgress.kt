package de.nicidienase.chaosflix.common.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Created by felix on 06.04.17.
 */

@Entity
class PlaybackProgress (@PrimaryKey var eventId: Int = 0, var progress: Long = 0){}
