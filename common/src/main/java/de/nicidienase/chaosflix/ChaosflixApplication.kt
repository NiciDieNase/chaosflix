package de.nicidienase.chaosflix

import android.app.Application

class ChaosflixApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        StageInit.init(this)
        LibsInit.init(this)
        BuildTypeInit.init(this)
    }
}
