package de.nicidienase.chaosflix.common.eventimport

data class FahrplanExport(
    val conference: String,
    val lectures: List<FahrplanLecture>
)