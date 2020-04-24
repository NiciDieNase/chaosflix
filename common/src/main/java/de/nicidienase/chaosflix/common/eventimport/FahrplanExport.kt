package de.nicidienase.chaosflix.common.eventimport

import androidx.annotation.Keep

@Keep
data class FahrplanExport(
    val conference: String,
    val lectures: List<FahrplanLecture>
)
