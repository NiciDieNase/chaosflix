import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.AppCenterService
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.appcenter.distribute.Distribute
import de.nicidienase.chaosflix.BuildConfig
import de.nicidienase.chaosflix.ChaosflixApplication

fun libsInit(application: ChaosflixApplication) {
    val modules: List<Class<out AppCenterService>>
    if (BuildConfig.FLAVOR_stage == "prod") {
        modules = listOf(Analytics::class.java, Crashes::class.java)
    } else {
        modules = listOf(Analytics::class.java, Crashes::class.java, Distribute::class.java)
    }
    AppCenter.start(application, BuildConfig.APPCENTER_ID, *(modules.toTypedArray()))
}