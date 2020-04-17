package de.nicidienase.chaosflix.touch.browse.eventslist

import androidx.core.view.contains
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import de.nicidienase.chaosflix.common.mediadata.MediaRepository
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.touch.databinding.FragmentEventsListBinding

class ConferenceEventListFragment : EventsListFragment() {

    private val args: ConferenceEventListFragmentArgs by navArgs()
    private val filterTextChip: Chip by lazy {
        Chip(requireContext()).apply {
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                viewModel.filterText.postValue("")
            }
        }
    }

    override fun navigateToDetails(event: Event) {
        findNavController().navigate(ConferenceEventListFragmentDirections.actionEventsListFragmentToEventDetailsFragment(eventGuid = event.guid))
    }

    override fun setupEvents(binding: FragmentEventsListBinding) {
        args.conference.let { conference ->
            activity?.actionBar?.title = conference.acronym
//            setupToolbar(binding.incToolbar.toolbar, conference.title, false)
//            eventAdapter.setShowTags(conference.getTagsUsefull())
//            viewModel.getEventsforConference(conference).observe(viewLifecycleOwner, Observer { events: List<Event>? ->
            viewModel.getFilteredEvents(conference).observe(viewLifecycleOwner, Observer { events: List<Event>? ->
                if (events != null) {
                    setEvents(events)
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
            viewModel.filterText.observe(viewLifecycleOwner, Observer { filterText ->
                val chip = filterTextChip
                if (filterText.isNullOrBlank()) {
                    binding.filterChipGroup.removeView(chip)
                } else {
                    chip.text = "Filter: $filterText"
                    if (!binding.filterChipGroup.contains(filterTextChip)) {
                        binding.filterChipGroup.addView(filterTextChip)
                    }
                }
            })
        }
        binding.swipeRefreshLayout.isEnabled = true
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.updateEventsForConference(args.conference)
        }
    }
}
