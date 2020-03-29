package de.nicidienase.chaosflix.touch

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import de.nicidienase.chaosflix.common.viewmodel.BrowseViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.touch.databinding.ActivityNavigationBinding

class NavigationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host)
        binding.bottomNavigation.setupWithNavController(navController)

        val appBarConfiguration =
            AppBarConfiguration(setOf(R.id.mediathekFragment, R.id.livestreamListFragment, R.id.conferencesTabBrowseFragment))
//                AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        setSupportActionBar(binding.toolbar)

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            if (noToolbarDestinations.contains(destination.id)) {
                supportActionBar?.hide()
                binding.toolbar.visibility = View.GONE
            } else {
                supportActionBar?.show()
                binding.toolbar.visibility = View.VISIBLE
            }
            if (noBottomNavDestinations.contains(destination.id)) {
                binding.bottomNavigation.visibility = View.GONE
            } else {
                binding.bottomNavigation.visibility = View.VISIBLE
            }
        }

        val viewModel = ViewModelProvider(this, ViewModelFactory.getInstance(this)).get(BrowseViewModel::class.java)
        viewModel.getLivestreams().observe(this, Observer {
            if (it.isEmpty()) {
                binding.bottomNavigation.removeBadge(R.id.livestreamListFragment)
            } else {
                val roomCount = it.flatMap { it.groups.map { it.rooms.size } }.reduce { acc: Int, i: Int -> acc + i }
                binding.bottomNavigation.getOrCreateBadge(R.id.livestreamListFragment).number = roomCount
            }
        })

        if (Intent.ACTION_SEARCH == intent?.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                findNavController(R.id.nav_host).navigate(R.id.searchFragment, bundleOf("query" to query))
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (Intent.ACTION_SEARCH == intent?.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                findNavController(R.id.nav_host).navigate(R.id.searchFragment, bundleOf("query" to query))
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> findNavController(R.id.nav_host).navigateUp()
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private val TAG = NavigationActivity::class.java.simpleName

        private val noToolbarDestinations = listOf(R.id.exoPlayerFragment, R.id.eventDetailsFragment)
        private val noBottomNavDestinations = listOf(R.id.exoPlayerFragment)

        fun launch(context: Context) {
            context.startActivity(Intent(context, NavigationActivity::class.java))
        }
    }
}
