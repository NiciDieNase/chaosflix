package de.nicidienase.chaosflix.touch.browse

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import android.transition.TransitionInflater
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.StreamUrl
import de.nicidienase.chaosflix.common.viewmodel.BrowseViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.touch.NavigationActivity
import de.nicidienase.chaosflix.touch.OnEventSelectedListener
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.touch.about.AboutActivity
import de.nicidienase.chaosflix.touch.browse.cast.CastService
import de.nicidienase.chaosflix.touch.browse.download.DownloadsListFragment
import de.nicidienase.chaosflix.touch.browse.eventslist.EventsListActivity
import de.nicidienase.chaosflix.touch.browse.eventslist.EventsListFragment
import de.nicidienase.chaosflix.touch.browse.streaming.LivestreamListFragment
import de.nicidienase.chaosflix.touch.browse.streaming.StreamingItem
import de.nicidienase.chaosflix.touch.databinding.ActivityBrowseBinding
import de.nicidienase.chaosflix.touch.eventdetails.EventDetailsActivity
import de.nicidienase.chaosflix.touch.playback.PlayerActivity
import de.nicidienase.chaosflix.touch.settings.SettingsActivity

class BrowseActivity : AppCompatActivity(),
        ConferencesTabBrowseFragment.OnInteractionListener,
        LivestreamListFragment.InteractionListener,
        OnEventSelectedListener {

    private var drawerOpen: Boolean = false

    private val TAG = BrowseActivity::class.simpleName

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var binding: ActivityBrowseBinding

    private lateinit var viewModel: BrowseViewModel

    protected val numColumns: Int
        get() = resources.getInteger(R.integer.num_columns)

    private lateinit var castService: CastService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_browse)

        castService = CastService(this)

        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_recordings -> showConferencesFragment()
                R.id.nav_bookmarks -> showBookmarksFragment()
                R.id.nav_inprogress -> showInProgressFragment()
                R.id.nav_about -> showAboutPage()
                R.id.nav_streams -> showStreamsFragment()
                R.id.nav_downloads -> showDownloadsFragment()
                R.id.nav_preferences -> showSettingsPage()
                R.id.nav_new_navigation -> {
                    startActivity(Intent(this, NavigationActivity::class.java))
                    finish()
                }
                else -> Snackbar.make(binding.drawerLayout, "Not implemented yet", Snackbar.LENGTH_SHORT).show()
            }
            binding.drawerLayout.closeDrawers()
            true
        }

        if (savedInstanceState == null) {
            showConferencesFragment()
        }
        viewModel = ViewModelProviders.of(this, ViewModelFactory.getInstance(this)).get(BrowseViewModel::class.java)

        viewModel.state.observe(this, Observer { event ->
            if (event == null) {
                return@Observer
            }
            val errorMessage = event.error
            if (errorMessage != null) {
                Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_SHORT).show()
            }
            when (event.state) {
                BrowseViewModel.State.ShowEventDetails -> {
                    event.data?.let {
                        EventDetailsActivity.launch(this, it)
                    }
                }
            }
        })
    }

    fun setupDrawerToggle(toolbar: Toolbar?) {
        if (toolbar != null) {
            drawerToggle = object : ActionBarDrawerToggle(this, binding.drawerLayout,
                    toolbar, R.string.drawer_open, R.string.drawer_close) {
                override fun onDrawerOpened(drawerView: View) {
                    super.onDrawerOpened(drawerView)
                    drawerOpen = true
                }

                override fun onDrawerClosed(drawerView: View) {
                    super.onDrawerClosed(drawerView)
                    drawerOpen = false
                }
            }
        } else {
            drawerToggle = object : ActionBarDrawerToggle(this, binding.drawerLayout,
                    R.string.drawer_open, R.string.drawer_close) {
                override fun onDrawerOpened(drawerView: View) {
                    super.onDrawerOpened(drawerView)
                    drawerOpen = true
                }

                override fun onDrawerClosed(drawerView: View) {
                    super.onDrawerClosed(drawerView)
                    drawerOpen = false
                }
            }
        }
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menu?.let {
            castService.addMediaRouteMenuItem(it)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onConferenceSelected(conference: Conference) {
        EventsListActivity.start(this, conference)
    }

    override fun onStreamSelected(streamingItem: StreamingItem) {
        val entries = HashMap<String, StreamUrl>()

        if (castService.connected) {
            val hdStreams = streamingItem.room.streams // .filter { it.slug.startsWith("hd-") }
            Log.i(TAG, "found ${hdStreams.size} suitable streams, starting selection")
            if (hdStreams.size > 1) {
                val dialog = AlertDialog.Builder(this)
                        .setTitle(getString(R.string.select_stream))
                        .setItems(hdStreams.map { it.display }.toTypedArray()) { _, i ->
                            val stream = hdStreams[i]
                            val keys = stream.urls.keys.toTypedArray()
                            val dialog = AlertDialog.Builder(this)
                                    .setTitle(this.getString(R.string.select_stream))
                                    .setItems(keys) { _: DialogInterface?, which: Int ->
                                        val streamUrl = stream.urls[keys[which]]
                                        if (streamUrl != null) {
                                            castService.castStream(streamingItem, streamUrl, keys[which])
                                        } else {
                                            Snackbar.make(binding.root, "could not play stream", Snackbar.LENGTH_SHORT).show()
                                        }
                                    }
                                    .create()
                            dialog.show()
                        }
                        .create()
                dialog.show()
            } else {
                Log.i(TAG, "Found no HD-Stream")
            }
        } else {
            val dashStreams = streamingItem.room.streams.filter { it.slug == "dash-native" }
            if (dashStreams.size > 0 &&
                    viewModel.getAutoselectStream()) {
                playStream(streamingItem.conference.conference,
                        streamingItem.room.display,
                        dashStreams.first().urls["dash"]
                )
            } else {
                streamingItem.room.streams.flatMap { stream ->
                    stream.urls.map { entry ->
                        entries.put(stream.slug + " " + entry.key, entry.value)
                    }
                }

                val builder = AlertDialog.Builder(this)
                val strings = entries.keys.sorted().toTypedArray()
                builder.setTitle(getString(R.string.select_stream))
                        .setItems(strings) { _, i ->
                            Toast.makeText(this, strings[i], Toast.LENGTH_LONG).show()
                            playStream(
                                    streamingItem.conference.conference,
                                    streamingItem.room.display,
                                    entries[strings[i]])
                        }
                builder.create().show()
            }
        }
    }

    private fun playStream(conference: String, room: String, streamUrl: StreamUrl?) {
        if (streamUrl != null) {
            PlayerActivity.launch(this, conference, room, streamUrl)
        }
    }

    private fun showConferencesFragment() {
        showFragment(ConferencesTabBrowseFragment.newInstance(numColumns), "conferences")
    }

    private fun showBookmarksFragment() {
        val bookmarksFragment = EventsListFragment.newInstance(EventsListFragment.TYPE_BOOKMARKS, null, numColumns)
        showFragment(bookmarksFragment, "bookmarks")
    }

    private fun showInProgressFragment() {
        val progressEventsFragment = EventsListFragment.newInstance(EventsListFragment.TYPE_IN_PROGRESS, null, numColumns)
        showFragment(progressEventsFragment, "in_progress")
    }

    private fun showStreamsFragment() {
        val fragment = LivestreamListFragment.newInstance(numColumns)
        showFragment(fragment, "streams")
    }

    private fun showDownloadsFragment() {
        val fragment = DownloadsListFragment.getInstance(numColumns)
        showFragment(fragment, "downloads")
    }

    private fun showSettingsPage() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun showAboutPage() {
        val intent = Intent(this, AboutActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        if (drawerOpen) {
            binding.drawerLayout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }

    protected fun showFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        val fm = supportFragmentManager
        val oldFragment = fm.findFragmentById(R.id.fragment_container)

        val transitionInflater = TransitionInflater.from(this)
        if (oldFragment != null) {
            if (oldFragment.tag.equals(tag)) {
                return
            }
            oldFragment.exitTransition = transitionInflater.inflateTransition(android.R.transition.fade)
        }
        fragment.enterTransition = transitionInflater.inflateTransition(android.R.transition.fade)

//        val slideTransition = Slide(Gravity.RIGHT)
//        fragment.enterTransition = slideTransition

        val ft = fm.beginTransaction()
        ft.replace(R.id.fragment_container, fragment, tag)
        ft.setReorderingAllowed(true)
        ft.setTransition(androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        ft.commit()
    }

    override fun onEventSelected(event: Event) {
        EventDetailsActivity.launch(this, event)
    }

    companion object {
        fun launch(context: Context) {
            context.startActivity(Intent(context, BrowseActivity::class.java))
        }
    }
}
