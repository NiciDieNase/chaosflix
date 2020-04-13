package de.nicidienase.chaosflix.leanback.conferences

import android.os.Bundle
import de.nicidienase.chaosflix.leanback.R
import de.nicidienase.chaosflix.leanback.recommendations.RecommendationBroadcastReceiver

class ConferencesActivity : androidx.fragment.app.FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conferences_browse)

        RecommendationBroadcastReceiver.setup(this)
    }

    companion object {
        private val TAG = ConferencesActivity::class.java.simpleName
    }
}
