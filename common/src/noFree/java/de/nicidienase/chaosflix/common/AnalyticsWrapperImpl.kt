package de.nicidienase.chaosflix.common

import android.app.Application
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.AppCenterService
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.appcenter.distribute.Distribute
import de.nicidienase.chaosflix.BuildConfig

object AnalyticsWrapperImpl: AnalyticsWrapper {
    override fun init(application: Application) {
        val modules: List<Class<out AppCenterService>>
        if (BuildConfig.BUILD_TYPE == "devNoFreeRelease") {
            modules = listOf(Analytics::class.java, Crashes::class.java, Distribute::class.java)
        } else {
            modules = listOf(Analytics::class.java, Crashes::class.java)
        }
        AppCenter.start(application, BuildConfig.APPCENTER_ID, *(modules.toTypedArray()))
    }

    override fun startAnalytics() {
        Analytics.setEnabled(true)
//        Crashes.setEnabled(true)
    }

    override fun stopAnalytics() {
        Analytics.setEnabled(false)
//        Crashes.setEnabled(false)
    }
}
