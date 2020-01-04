package de.nicidienase.chaosflix

import com.facebook.stetho.Stetho

object BuildTypeInit : ChaosflixInitializer {
    override fun init(chaosflixApplication: ChaosflixApplication) {
        Stetho.initializeWithDefaults(chaosflixApplication)
    }
}