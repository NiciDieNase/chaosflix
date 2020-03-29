package de.nicidienase.chaosflix.touch.browse.mediathek

import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.touch.browse.eventslist.EventsListFragment
import de.nicidienase.chaosflix.touch.databinding.FragmentEventsListBinding

class InProgressListFragment : EventsListFragment() {

    override fun setupEvents(binding: FragmentEventsListBinding) {
        viewModel.getInProgressEvents().observe(viewLifecycleOwner, Observer { persistentEvents: List<Event>? ->
            persistentEvents?.let { setEvents(it) }
        })
    }

    override fun navigateToDetails(event: Event) {
        findNavController().navigate(
                MediathekFragmentDirections.actionMyChaosflixFragmentToEventDetailsFragment(eventGuid = event.guid))
    }
}
