package de.nicidienase.chaosflix.leanback.conferences

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import de.nicidienase.chaosflix.common.ChaosflixPreferenceManager
import de.nicidienase.chaosflix.common.viewmodel.BrowseViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.leanback.ChannelManager
import de.nicidienase.chaosflix.leanback.R
import kotlinx.coroutines.launch

class ConferencesActivity : androidx.fragment.app.FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conferences_browse)
    }

    override fun onStart() {
        super.onStart()

        val prefs = ChaosflixPreferenceManager(PreferenceManager.getDefaultSharedPreferences(applicationContext))

        val viewmodel = ViewModelProvider(
                this, ViewModelFactory.getInstance(this)).get(BrowseViewModel::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            lifecycleScope.launch {
                ChannelManager.setupChannels(this@ConferencesActivity, viewmodel, prefs)
            }
        } else {
            setupRecommendations()
        }
    }

    private fun setupRecommendations() {
//        startService(Intent(this, ChaosRecommendationsService::class.java))
    }

    companion object {
        private val TAG = ConferencesActivity::class.java.simpleName
    }
}
