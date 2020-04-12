package de.nicidienase.chaosflix.leanback

import androidx.annotation.DrawableRes

enum class SelectableContentItem(val title: String, @DrawableRes val icon: Int, val data: Any? = null) {
    Settings("Settings", R.drawable.ic_settings),
    About("About", R.drawable.ic_info),
    LeakCanary("Leak Canary", R.drawable.ic_warning),
    AddRecommendations("Update Recommendations", R.drawable.ic_refresh),
    UpdateStreams("Update Streams", R.drawable.ic_refresh)
}
