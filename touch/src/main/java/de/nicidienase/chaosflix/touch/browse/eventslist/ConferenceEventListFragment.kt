package de.nicidienase.chaosflix.touch.browse.eventslist

import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.nicidienase.chaosflix.common.mediadata.MediaRepository
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.touch.databinding.FragmentEventsListBinding

class ConferenceEventListFragment: EventsListFragment() {

	private val args: ConferenceEventListFragmentArgs by navArgs()

	override fun navigateToDetails(event: Event) {
		findNavController().navigate(ConferenceEventListFragmentDirections.actionEventsListFragmentToEventDetailsFragment(event))
	}

	override fun setupEvents(binding: FragmentEventsListBinding) {
		args.conference.let { conference ->
			setupToolbar(binding.incToolbar.toolbar, conference.title, false)
			// 				eventAdapter.setShowTags(conference.getTagsUsefull());
			viewModel.getEventsforConference(conference).observe(viewLifecycleOwner, Observer { events: List<Event>? ->
				if (events != null) {
					setEvents(events)
					setLoadingOverlayVisibility(false)
				}
			})
			viewModel.updateEventsForConference(conference).observe(viewLifecycleOwner, Observer { state ->
				when (state.state) {
					MediaRepository.State.RUNNING -> setRefreshing(binding, true)
					MediaRepository.State.DONE -> setRefreshing(binding, false)
				}
				state.error?.let {
					showSnackbar(it, binding)
				}
			})
		}
		binding.swipeRefreshLayout.isEnabled = true
		binding.swipeRefreshLayout.setOnRefreshListener {
			args.conference?.let { viewModel.updateEventsForConference(it) }
		}
	}
}