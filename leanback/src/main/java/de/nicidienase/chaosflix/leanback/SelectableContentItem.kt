package de.nicidienase.chaosflix.leanback

import androidx.annotation.DrawableRes

enum class SelectableContentItem(val title: String, @DrawableRes val icon: Int, val data: Any? = null) {
    Settings("Settings", android.R.drawable.ic_menu_preferences),
    About("About", android.R.drawable.ic_menu_info_details)
}
