package de.nicidienase.chaosflix

import android.app.Application

class ChaosflixApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        stageInit(this)
        libsInit(this)
        buildTypeInit(this)
    }
}
