package de.nicidienase.chaosflix.common.entities

import android.arch.persistence.room.Entity

import org.joda.time.DateTime

import java.util.Date

/**
 * Created by felix on 19.04.17.
 */

@Entity
class WatchlistItem (var id: Int = 0, var eventId: Int = id, var added: DateTime = DateTime(Date())) {}
