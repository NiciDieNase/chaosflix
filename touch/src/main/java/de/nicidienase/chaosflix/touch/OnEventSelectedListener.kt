package de.nicidienase.chaosflix.touch

import android.view.View
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentEvent

interface OnEventSelectedListener {
	fun onEventSelected(event: PersistentEvent, v: View);
}