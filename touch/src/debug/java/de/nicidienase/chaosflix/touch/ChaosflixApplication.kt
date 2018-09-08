package de.nicidienase.chaosflix.touch

import android.app.Application
import android.content.Context
import com.facebook.stetho.Stetho

class ChaosflixApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        APPLICATION_CONTEXT = this
        Stetho.initializeWithDefaults(this)
    }

    companion object {
        lateinit var APPLICATION_CONTEXT: Context
    }
}
