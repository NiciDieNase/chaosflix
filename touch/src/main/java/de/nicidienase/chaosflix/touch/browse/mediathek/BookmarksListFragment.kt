package de.nicidienase.chaosflix.touch.browse.mediathek

import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.touch.browse.eventslist.EventsListFragment
import de.nicidienase.chaosflix.touch.databinding.FragmentEventsListBinding

class BookmarksListFragment : EventsListFragment() {

	override fun setupEvents(binding: FragmentEventsListBinding) {
		setupToolbar(binding.incToolbar.toolbar, R.string.bookmarks)
		binding.incToolbar.toolbar.visibility = View.GONE
		viewModel.getBookmarkedEvents().observe(viewLifecycleOwner, Observer { persistentEvents: List<Event>? ->
			setLoadingOverlayVisibility(false)
			persistentEvents?.let { setEvents(it) }
		})
	}

	override fun navigateToDetails(event: Event) {
		findNavController().navigate(
				MyChaosflixFragmentDirections.actionMyChaosflixFragmentToEventDetailsFragment(event = event))
	}
}