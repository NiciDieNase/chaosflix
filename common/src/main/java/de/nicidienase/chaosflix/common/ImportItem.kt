package de.nicidienase.chaosflix.common

import de.nicidienase.chaosflix.common.eventimport.FahrplanLecture
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event

data class ImportItem(
    val lecture: FahrplanLecture,
    var event: Event?,
    var selected: Boolean = false
)