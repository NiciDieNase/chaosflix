package de.nicidienase.chaosflix.leanback.conferences

import android.os.Bundle
import de.nicidienase.chaosflix.leanback.R

class ConferencesActivity : androidx.fragment.app.FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conferences_browse)
    }

    companion object {
        private val TAG = ConferencesActivity::class.java.simpleName
    }
}
