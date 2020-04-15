package de.nicidienase.chaosflix.touch.search

import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.touch.browse.eventslist.EventsListFragment
import de.nicidienase.chaosflix.touch.databinding.FragmentEventsListBinding

class SearchFragment : EventsListFragment() {
    private val args: SearchFragmentArgs by navArgs()

    override fun navigateToDetails(event: Event) {
        findNavController().navigate(SearchFragmentDirections.actionSearchFragmentToEventDetailsFragment(eventGuid = event.guid))
    }

    override fun setupEvents(binding: FragmentEventsListBinding) {
        val adapter = SearchResultAdapter {
            navigateToDetails(it)
        }
        binding.list.adapter = adapter
        viewModel.searchEventsPaged(args.query).observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }
}

