package de.nicidienase.chaosflix.leanback

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import de.nicidienase.chaosflix.leanback.conferences.ConferencesActivity

class SplashActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)


        ConferencesActivity.launch(this)
        finish()
    }
}