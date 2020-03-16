package de.nicidienase.chaosflix.touch

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.touch.browse.ConferencesTabBrowseFragment
import de.nicidienase.chaosflix.touch.browse.streaming.LivestreamListFragment
import de.nicidienase.chaosflix.touch.browse.streaming.StreamingItem

class NavigationActivity : AppCompatActivity(),
        ConferencesTabBrowseFragment.OnInteractionListener,
        LivestreamListFragment.InteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navController = findNavController(R.id.nav_host)
        bottomNavigationView.setupWithNavController(navController)
    }

    override fun onConferenceSelected(conference: Conference?) {
        // TODO move navigation to fragment
        Log.d(TAG, "Should navigate to ${conference?.acronym}")
    }

    override fun onStreamSelected(streamingItem: StreamingItem) {
        // TODO move navigation to fragment
        Log.d(TAG, "Should navigate to $streamingItem")
    }

    companion object {
        private val TAG = NavigationActivity::class.java.simpleName
    }
}
