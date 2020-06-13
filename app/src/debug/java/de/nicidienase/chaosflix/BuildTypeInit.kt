package de.nicidienase.chaosflix

import android.app.Application
import com.facebook.stetho.Stetho

object BuildTypeInit : ChaosflixInitializer {
    override fun init(application: Application) {
        Stetho.initializeWithDefaults(application)
    }
}
