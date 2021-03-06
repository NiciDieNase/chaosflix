package de.nicidienase.chaosflix.common

import android.app.Application
import android.util.Log
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.AppCenterService
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import de.nicidienase.chaosflix.BuildConfig

object AnalyticsWrapperImpl : AnalyticsWrapper {
    override fun init(application: Application) {
        val modules: List<Class<out AppCenterService>>
        modules = listOf(Analytics::class.java, Crashes::class.java)
        AppCenter.start(application, BuildConfig.APPCENTER_ID, *(modules.toTypedArray()))
    }

    override fun startAnalytics() {
        Analytics.setEnabled(true)
        Crashes.setEnabled(true)
    }

    override fun stopAnalytics() {
        Analytics.setEnabled(false)
        Crashes.setEnabled(false)
    }

    override fun addAnalyticsEvent(event: String, params: Map<String, String>) {
        Log.i(TAG, "Tracking analytics event $event with parameters $params")
        Analytics.trackEvent(event, params)
    }

    override fun trackException(exception: Exception) {
        Crashes.trackError(exception)
    }

    private val TAG = AnalyticsWrapperImpl::class.java.simpleName
}
