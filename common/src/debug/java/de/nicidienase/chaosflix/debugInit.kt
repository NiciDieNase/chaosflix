package de.nicidienase.chaosflix

import com.facebook.stetho.Stetho

fun buildTypeInit(application: ChaosflixApplication) {
    Stetho.initializeWithDefaults(application)
}