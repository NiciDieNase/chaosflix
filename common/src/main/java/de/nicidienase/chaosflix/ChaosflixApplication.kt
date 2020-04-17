package de.nicidienase.chaosflix

import android.app.Application
import de.nicidienase.chaosflix.common.DarkmodeUtil

class ChaosflixApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        StageInit.init(this)
        LibsInit.init(this)
        BuildTypeInit.init(this)
        DarkmodeUtil.init(this)
    }
}
