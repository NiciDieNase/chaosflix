package de.nicidienase.chaosflix.leanback

import androidx.leanback.widget.DiffCallback
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event

object DiffCallbacks {
    val eventDiffCallback = object : DiffCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.guid == newItem.guid
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.guid == newItem.guid
        }
    }

    val conferenceDiffCallback = object : DiffCallback<Conference>() {
        override fun areItemsTheSame(oldItem: Conference, newItem: Conference): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Conference, newItem: Conference): Boolean {
            return oldItem.updatedAt == newItem.updatedAt
        }
    }
}
