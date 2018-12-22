package de.nicidienase.chaosflix.leanback.events

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.sync.Downloader
import de.nicidienase.chaosflix.common.util.ConferenceUtil
import de.nicidienase.chaosflix.common.viewmodel.BrowseViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.leanback.BrowseErrorFragment
import de.nicidienase.chaosflix.leanback.R

class EventsActivity : FragmentActivity() {

	lateinit var viewModel: BrowseViewModel

	private lateinit var conference: Conference

	private var fragment: EventsFragment? = null

	public override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		conference = intent.getParcelableExtra<Conference>(CONFERENCE)
		setContentView(R.layout.activity_events_browse)

		viewModel = ViewModelProviders.of(this, ViewModelFactory(this)).get(BrowseViewModel::class.java)

		if(savedInstanceState == null){

		}
	}

	override fun onStart() {
		super.onStart()
		var errorFragment: BrowseErrorFragment? = null
		viewModel.updateEventsForConference(conference).observe(this, Observer { event ->
			when (event?.state) {
				Downloader.DownloaderState.RUNNING -> {
					supportFragmentManager?.let {
						errorFragment = BrowseErrorFragment.showErrorFragment(it, EventsGridBrowseFragment.FRAGMENT)
					}
				}
				Downloader.DownloaderState.DONE -> {
					if (event.error != null) {
						val errorMessage = event.error ?: "Error refreshing events"
						errorFragment?.setErrorContent(errorMessage)
					} else {
						errorFragment?.dismiss()
					}
				}
			}
		})
		viewModel.getEventsforConference(conference).observe(this, Observer { events ->
			events?.let { events ->
				val tagList = events.map { it.tags ?: emptyArray() }.toTypedArray().flatten()
				val filteredTags = tagList.filterNot { it.matches("\\d+".toRegex()) }.filterNot { it==conference.acronym }.toSet()
				updateFragment(filteredTags.size > 1)
				(fragment as EventsFragment).updateEvents(conference, events)
			}
		})
	}

	private fun updateFragment(tagsUseful: Boolean){
		if((tagsUseful && fragment is EventsRowsBrowseFragment)
				|| (!tagsUseful && fragment is EventsGridBrowseFragment) ){
			return
		}
		val newFragment: Fragment = //EventsRowsBrowseFragment.create(conference)
				if (tagsUseful) {
					EventsRowsBrowseFragment.create(conference)
				} else {
					EventsGridBrowseFragment.create(conference)
				}
		supportFragmentManager.beginTransaction().replace(R.id.browse_fragment, newFragment).commit()

		fragment = newFragment as EventsFragment
	}

	interface EventsFragment{
		fun updateEvents(conference: Conference, events: List<Event>)
	}

	companion object {
		@JvmStatic
		val CONFERENCE = "conference"
		@JvmStatic
		val SHARED_ELEMENT_NAME = "shared_element"

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
