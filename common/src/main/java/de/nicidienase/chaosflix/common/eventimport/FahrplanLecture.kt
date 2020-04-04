package de.nicidienase.chaosflix.common.eventimport

import com.google.gson.annotations.SerializedName

data class FahrplanLecture(
		@SerializedName("lecture_id")
		var lectureId: String? = null,
		var title: String,
		var subtitle: String? = null,
		var links: String? = null,
		var track: String? = null,
		var description: String? = null
)