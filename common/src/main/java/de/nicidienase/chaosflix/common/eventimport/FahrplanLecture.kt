package de.nicidienase.chaosflix.common.eventimport

data class FahrplanLecture(
    var lectureId: String? = null,
    var title: String,
    var subtitle: String? = null,
    var links: String? = null,
    var track: String? = null,
    var description: String? = null
)