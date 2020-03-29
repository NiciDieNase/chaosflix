package de.nicidienase.chaosflix.touch.browse

import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.touch.browse.eventslist.EventsListFragment
import de.nicidienase.chaosflix.touch.databinding.FragmentEventsListBinding
import kotlinx.coroutines.launch

class SearchFragment: EventsListFragment() {
	private val args: SearchFragmentArgs by navArgs()

	override fun navigateToDetails(event: Event) {
		findNavController().navigate(SearchFragmentDirections.actionSearchFragmentToEventDetailsFragment(eventGuid = event.guid))
	}

	override fun setupEvents(binding: FragmentEventsListBinding) {

		lifecycleScope.launch {
			val searchEvents = viewModel.searchEvents(args.query)
			setEvents(searchEvents)
		}
	}
}