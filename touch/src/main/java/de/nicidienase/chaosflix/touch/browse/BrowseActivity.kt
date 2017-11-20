package de.nicidienase.chaosflix.touch.browse

import android.content.Intent
import android.content.res.Configuration
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.PersistableBundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.transition.TransitionInflater
import android.view.MenuItem
import android.view.View
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.entities.streaming.LiveConference
import de.nicidienase.chaosflix.common.entities.streaming.Stream
import de.nicidienase.chaosflix.databinding.ActivityBrowseBinding
import de.nicidienase.chaosflix.touch.OnEventSelectedListener
import de.nicidienase.chaosflix.touch.activities.AboutActivity
import de.nicidienase.chaosflix.touch.browse.download.DownloadsListFragment
import de.nicidienase.chaosflix.touch.browse.eventslist.EventsListActivity
import de.nicidienase.chaosflix.touch.browse.eventslist.EventsListFragment
import de.nicidienase.chaosflix.touch.browse.streaming.LivestreamListFragment
import de.nicidienase.chaosflix.touch.eventdetails.EventDetailsActivity

class BrowseActivity : AppCompatActivity(),
        ConferencesTabBrowseFragment.OnInteractionListener,
        LivestreamListFragment.InteractionListener,
        DownloadsListFragment.InteractionListener,
        OnEventSelectedListener {
    private var drawerOpen: Boolean = false

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var binding: ActivityBrowseBinding

    protected val numColumns: Int
        get() = resources.getInteger(R.integer.num_columns)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_browse)

        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_recordings -> showConferencesFragment()
                R.id.nav_bookmarks -> showBookmarksFragment()
                R.id.nav_inprogress -> showInProgressFragment()
                R.id.nav_about -> showAboutPage()
                R.id.nav_streams -> showStreamsFragment()
                R.id.nav_downloads -> showDownloadsFragment()
                R.id.nav_preferences -> Snackbar.make(binding.drawerLayout, "Not implemented yet", Snackbar.LENGTH_SHORT).show()
                else -> Snackbar.make(binding.drawerLayout, "Not implemented yet", Snackbar.LENGTH_SHORT).show()
            }
            binding.drawerLayout.closeDrawers()
            true
        }

        if (savedInstanceState == null) {
            showConferencesFragment()
        }
    }

    fun setupDrawerToggle(toolbar: Toolbar?) {
        if(toolbar != null){
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
        showFragment(ConferencesTabBrowseFragment.newInstance(numColumns), "conferences")
    }

    private fun showBookmarksFragment() {
        val bookmarksFragment = EventsListFragment.newInstance(EventsListFragment.BOOKMARKS_LIST_ID, numColumns)
        showFragment(bookmarksFragment, "bookmarks")
    }

    private fun showInProgressFragment() {
        val progressEventsFragment = EventsListFragment.newInstance(EventsListFragment.IN_PROGRESS_LIST_ID, numColumns)
        showFragment(progressEventsFragment, "in_progress")
    }

    private fun showStreamsFragment() {
        val fragment = LivestreamListFragment()
        showFragment(fragment, "streams")
    }

    private fun showDownloadsFragment() {
        showFragment(DownloadsListFragment(),"downloads")
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

    protected fun showFragment(fragment: Fragment, tag: String) {
        val fm = supportFragmentManager
        val oldFragment = fm.findFragmentById(R.id.fragment_container)

        val transitionInflater = TransitionInflater.from(this)
        if (oldFragment != null) {
            if(oldFragment.tag.equals(tag)){
                return
            }
            oldFragment.exitTransition = transitionInflater.inflateTransition(android.R.transition.fade)
        }
        fragment.enterTransition = transitionInflater.inflateTransition(android.R.transition.fade)

//        val slideTransition = Slide(Gravity.RIGHT)
//        fragment.enterTransition = slideTransition

        val ft = fm.beginTransaction()
        ft.replace(R.id.fragment_container, fragment,tag)
        ft.setReorderingAllowed(true)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        ft.commit()
    }

    override fun onEventSelected(event: PersistentEvent, v: View) {
        EventDetailsActivity.launch(this, event.eventId)
    }
}
