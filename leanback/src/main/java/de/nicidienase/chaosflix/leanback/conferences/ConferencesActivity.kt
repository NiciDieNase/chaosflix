package de.nicidienase.chaosflix.leanback.conferences

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import de.nicidienase.chaosflix.leanback.R

class ConferencesActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conferences_browse)
    }

    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, ConferencesActivity::class.java)
            context.startActivity(intent)
        }
    }
}
