package de.nicidienase.chaosflix

import com.facebook.stetho.Stetho

object BuildTypeInit : ChaosflixInitializer {
    override fun init(chaosflixApplication: de.nicidienase.chaosflix.ChaosflixApplication) {
        Stetho.initializeWithDefaults(chaosflixApplication)
    }
}
