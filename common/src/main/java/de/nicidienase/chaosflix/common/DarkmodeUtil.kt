package de.nicidienase.chaosflix.common

import android.content.Context
import android.os.Build
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import de.nicidienase.chaosflix.R

object DarkmodeUtil {

    fun init(context: Context) {
        val preferenceKeyDarkmode = context.getString(R.string.preference_key_darkmode_setting)
        val default = context.getString(R.string.system_default)
        val dark = context.getString(R.string.always_dark)
        val light = context.getString(R.string.always_light)
        val darkmodeSetting = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(preferenceKeyDarkmode, default)
        val setting = when (darkmodeSetting) {
            dark -> AppCompatDelegate.MODE_NIGHT_YES
            light -> AppCompatDelegate.MODE_NIGHT_NO
            else -> if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            } else {
                AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            }
        }
        AppCompatDelegate.setDefaultNightMode(setting)
    }
}