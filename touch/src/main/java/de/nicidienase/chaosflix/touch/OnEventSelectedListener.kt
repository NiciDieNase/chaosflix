package de.nicidienase.chaosflix.touch

import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentEvent

interface OnEventSelectedListener {
	fun onEventSelected(event: PersistentEvent);
}