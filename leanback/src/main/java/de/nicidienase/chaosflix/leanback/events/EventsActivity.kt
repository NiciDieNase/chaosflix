package de.nicidienase.chaosflix.leanback.events

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.util.Log
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.sync.Downloader
import de.nicidienase.chaosflix.common.viewmodel.BrowseViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.leanback.BrowseErrorFragment
import de.nicidienase.chaosflix.leanback.R

class EventsActivity : FragmentActivity() {

	lateinit var viewModel: BrowseViewModel

	private lateinit var conference: Conference

	private var fragment: EventsFragment? = null

	var errorFragment: BrowseErrorFragment? = null

	public override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		conference = intent.getParcelableExtra<Conference>(CONFERENCE)
		setContentView(R.layout.activity_events_browse)
		viewModel = ViewModelProviders.of(this, ViewModelFactory(this)).get(BrowseViewModel::class.java)
	}

	override fun onStart() {
		super.onStart()
		viewModel.updateEventsForConference(conference).observe(this, Observer { event ->
			when (event?.state) {
				Downloader.DownloaderState.RUNNING -> {
					Log.i(TAG, "Refresh running")
					supportFragmentManager?.let {
						if(errorFragment == null){
							errorFragment = BrowseErrorFragment.showErrorFragment(it, R.id.browse_fragment)
						}
					}
				}
				Downloader.DownloaderState.DONE -> {
					Log.i(TAG, "Refresh done")
					if (event.error != null) {
						val errorMessage = event.error ?: "Error refreshing events"
						errorFragment?.setErrorContent(errorMessage, supportFragmentManager)
					} else {
						if(event.data?.isEmpty() ?: false){
							errorFragment?.setErrorContent("No Events found for this conference", supportFragmentManager)
						} else {
							errorFragment?.dismiss(supportFragmentManager)
						}
					}
				}
			}
		})
		viewModel.getEventsforConference(conference).observe(this, Observer { events ->
			events?.let { events ->
				if(events.size > 0){
					val tagList = events.map { it.tags ?: emptyArray() }.toTypedArray().flatten()
					val filteredTags = tagList.filterNot { it.matches("\\d+".toRegex()) }.filterNot { it == conference.acronym }.toSet()
					updateFragment(filteredTags.size > 1)
					(fragment as EventsFragment).updateEvents(conference, events)
				}
			}
		})
	}

	private fun updateFragment(tagsUseful: Boolean){
		if((tagsUseful && fragment is EventsRowsBrowseFragment)
				|| (!tagsUseful && fragment is EventsGridBrowseFragment) ){
			Log.i(TAG, "Fragment is up-to-date, returning")
			return
		}
		val newFragment: Fragment = //EventsRowsBrowseFragment.create(conference)
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

	interface EventsFragment{
		fun updateEvents(conference: Conference, events: List<Event>)
	}

	companion object {
		@JvmStatic
		val CONFERENCE = "conference"
		@JvmStatic
		val SHARED_ELEMENT_NAME = "shared_element"
		val TAG = EventsActivity::class.java.simpleName

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
