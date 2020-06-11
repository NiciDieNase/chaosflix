package de.nicidienase.chaosflix.common.mediadata.entities.eventinfo.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class VocEventDto(
        val name: String?,
        @SerializedName("short_name")
        val shortName: String?,
        val location: String?,
        @SerializedName("start_date")
        val startDate: String?, // Date: YYYY-MM-DD
        @SerializedName("end_date")
        val endDate: String?, // Date: YYYY-MM-DD
        val description: String?,
        @SerializedName("voc_wiki_path")
        val vocWikiPath: String?,
        val streaming: Boolean?,
        @SerializedName("planing_status")
        val planingStatus: String?,
        val cases: List<String>?,
        val buildup: String?, // Date: YYYY-MM-DD
        val teardown: String? // Date: YYYY-MM-DD
)