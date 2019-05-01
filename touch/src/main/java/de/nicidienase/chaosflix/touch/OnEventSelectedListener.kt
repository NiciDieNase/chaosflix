package de.nicidienase.chaosflix.touch

import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event

interface OnEventSelectedListener {
    fun onEventSelected(event: Event)
}