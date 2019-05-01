package de.nicidienase.chaosflix

import com.facebook.stetho.Stetho
import com.squareup.leakcanary.LeakCanary

fun buildTypeInit(application: ChaosflixApplication) {
    Stetho.initializeWithDefaults(application)
    if (!LeakCanary.isInAnalyzerProcess(application)) {
        // This process is dedicated to LeakCanary for heap analysis.
        // You should not init your app in this process.
        LeakCanary.install(application)
    }
}