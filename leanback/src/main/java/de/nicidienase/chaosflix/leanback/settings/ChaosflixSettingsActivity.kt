package de.nicidienase.chaosflix.leanback.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import de.nicidienase.chaosflix.leanback.R

class ChaosflixSettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    companion object {
        fun launch(context: Context) {
            context.startActivity(Intent(context, ChaosflixSettingsActivity::class.java))
        }
    }
}
