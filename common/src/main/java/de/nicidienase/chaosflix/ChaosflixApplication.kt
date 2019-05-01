package de.nicidienase.chaosflix

import android.app.Application
import com.facebook.stetho.Stetho
import com.squareup.leakcanary.LeakCanary
import libsInit
import stageInit

class ChaosflixApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        stageInit(this)
        libsInit(this)
        buildTypeInit(this)
    }
}
