import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import de.nicidienase.chaosflix.BuildConfig
import de.nicidienase.chaosflix.ChaosflixApplication

fun libsInit(application: ChaosflixApplication){
    AppCenter.start(application, BuildConfig.APPCENTER_ID,
        Analytics::class.java, Crashes::class.java)
}