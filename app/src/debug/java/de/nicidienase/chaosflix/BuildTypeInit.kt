package de.nicidienase.chaosflix

import android.app.Application
import com.facebook.stetho.Stetho

object BuildTypeInit : ChaosflixInitializer {
    override fun init(chaosflixApplication: Application) {
        Stetho.initializeWithDefaults(chaosflixApplication)
    }
}
