package de.nicidienase.chaosflix

import android.app.Application
import android.content.Context
import com.facebook.stetho.Stetho

class ChaosflixApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
    }
}
