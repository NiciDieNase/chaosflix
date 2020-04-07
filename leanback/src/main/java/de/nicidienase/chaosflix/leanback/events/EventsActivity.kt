package de.nicidienase.chaosflix.leanback.events

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.nicidienase.chaosflix.common.ChaosflixUtil
import de.nicidienase.chaosflix.common.mediadata.MediaRepository
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.viewmodel.BrowseViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.leanback.BrowseErrorFragment
import de.nicidienase.chaosflix.leanback.R

class EventsActivity : androidx.fragment.app.FragmentActivity() {

    lateinit var viewModel: BrowseViewModel

    private lateinit var conference: Conference

    private var fragment: EventsFragment? = null

    var errorFragment: BrowseErrorFragment? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        conference = intent.getParcelableExtra(CONFERENCE)
        setContentView(R.layout.activity_events_browse)
        viewModel = ViewModelProviders.of(this, ViewModelFactory.getInstance(this)).get(BrowseViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()
        viewModel.updateEventsForConference(conference).observe(this, Observer { event ->
            when (event?.state) {
                MediaRepository.State.RUNNING -> {
                    Log.i(TAG, "Refresh running")
                    if (errorFragment == null) {
                        errorFragment = BrowseErrorFragment.showErrorFragment(supportFragmentManager, R.id.browse_fragment)
                    }
                }
                MediaRepository.State.DONE -> {
                    Log.i(TAG, "Refresh done")
                    if (event.error != null) {
                        val errorMessage = event.error ?: "Error refreshing events"
                        errorFragment?.setErrorContent(errorMessage, supportFragmentManager)
                    } else {
                        if (event.data?.isEmpty() == true) {
                            errorFragment?.setErrorContent("No Events found for this conference", supportFragmentManager)
                        } else {
                            errorFragment?.dismiss(supportFragmentManager)
                        }
                    }
                }
            }
        })
        viewModel.getEventsforConference(conference).observe(this, Observer { events ->
            events?.let {
                if (it.isNotEmpty()) {
                    updateFragment(ChaosflixUtil.areTagsUsefull(events, conference.acronym))
                    (fragment as EventsFragment).updateEvents(conference, it)
                }
            }
        })
    }

    private fun updateFragment(tagsUseful: Boolean) {
        if ((tagsUseful && fragment is EventsRowsBrowseFragment) ||
                (!tagsUseful && fragment is EventsGridBrowseFragment)) {
            Log.i(TAG, "Fragment is up-to-date, returning")
            return
        }
        val newFragment: Fragment = // EventsRowsBrowseFragment.create(conference)
                if (tagsUseful) {
                    Log.i(TAG, "setting RowsFragment")
                    EventsRowsBrowseFragment.create(conference)
                } else {
                    Log.i(TAG, "setting gridFragment")
                    EventsGridBrowseFragment.create(conference)
                }
        errorFragment?.dismiss(supportFragmentManager) ?: Log.e(TAG, "Cannot dismiss, errorFragment is null")
        supportFragmentManager.beginTransaction().replace(R.id.browse_fragment, newFragment).commit()

        fragment = newFragment as EventsFragment
    }

    interface EventsFragment {
        fun updateEvents(conference: Conference, events: List<Event>)
    }

    companion object {
        @JvmStatic
        val CONFERENCE = "conference"
        @JvmStatic
        val SHARED_ELEMENT_NAME = "shared_element"
        private val TAG = EventsActivity::class.java.simpleName

        @JvmStatic
        @JvmOverloads
        fun start(context: Context, conference: Conference, transition: Bundle? = null) {
            val i = Intent(context, EventsActivity::class.java)
            i.putExtra(CONFERENCE, conference)
            if (transition != null) {
                context.startActivity(i, transition)
            } else {
                context.startActivity(i)
            }
        }
    }
}
