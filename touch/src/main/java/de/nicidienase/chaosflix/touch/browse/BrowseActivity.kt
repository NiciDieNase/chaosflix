package de.nicidienase.chaosflix.touch.browse

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.entities.streaming.LiveConference
import de.nicidienase.chaosflix.common.entities.streaming.Stream
import de.nicidienase.chaosflix.touch.OnEventSelectedListener
import de.nicidienase.chaosflix.touch.activities.AboutActivity
import de.nicidienase.chaosflix.touch.browse.eventslist.EventsListActivity
import de.nicidienase.chaosflix.touch.browse.eventslist.EventsListFragment
import de.nicidienase.chaosflix.touch.browse.streaming.LivestreamListFragment

class BrowseActivity : BrowseBaseActivity(),
        ConferencesTabBrowseFragment.OnInteractionListener,
        EventsListFragment.OnInteractionListener,
        LivestreamListFragment.InteractionListener,
        OnEventSelectedListener {
    private var drawerOpen: Boolean = false

    private lateinit var toolbar: Toolbar
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browse)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        drawerLayout = findViewById(R.id.drawer_layout)
        drawerToggle = object : ActionBarDrawerToggle(this, drawerLayout,
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
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_recordings -> showConferencesFragment()
                R.id.nav_bookmarks -> showBookmarksFragment()
                R.id.nav_inprogress -> showInProgressFragment()
                R.id.nav_about -> showAboutPage()
                R.id.nav_streams -> showStreamsFragmen()
                R.id.nav_preferences -> Snackbar.make(drawerLayout, "Not implemented yet", Snackbar.LENGTH_SHORT).show()
                else -> Snackbar.make(drawerLayout, "Not implemented yet", Snackbar.LENGTH_SHORT).show()
            }
            drawerLayout.closeDrawers()
            true
        }

        if (savedInstanceState == null) {
            showConferencesFragment()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onConferenceSelected(conferenceId: Long) {
        EventsListActivity.start(this, conferenceId)
    }

    override fun onStreamSelected(conference: LiveConference, stream: Stream) {
        TODO("not implemented")
    }

    private fun showConferencesFragment() {
        toolbar.setTitle(R.string.app_name)
        showFragment(ConferencesTabBrowseFragment.newInstance(numColumns))
    }

    private fun showBookmarksFragment() {
        toolbar.setTitle(R.string.bookmarks)
        val bookmarksFragment = EventsListFragment.newInstance(EventsListFragment.BOOKMARKS_LIST_ID, numColumns)
        showFragment(bookmarksFragment)
    }

    private fun showInProgressFragment() {
        toolbar.setTitle(R.string.continue_watching)
        val progressEventsFragment = EventsListFragment.newInstance(EventsListFragment.IN_PROGRESS_LIST_ID, numColumns)
        showFragment(progressEventsFragment)
    }

    private fun showStreamsFragmen() {
        toolbar.setTitle(getString(R.string.livestreams))
        val fragment = LivestreamListFragment()
        showFragment(fragment)
    }

    private fun showAboutPage() {
        val intent = Intent(this, AboutActivity::class.java)
        startActivity(intent)
    }

    override fun setToolbarTitle(title: String) {
        toolbar.title = title
    }

    override fun onBackPressed() {
        if (drawerOpen) {
            drawerLayout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }
}
