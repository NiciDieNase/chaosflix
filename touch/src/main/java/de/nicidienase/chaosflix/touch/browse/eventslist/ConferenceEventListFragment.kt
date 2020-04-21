package de.nicidienase.chaosflix.touch.browse.eventslist

import android.util.Log
import androidx.core.os.bundleOf
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
    private var filterTextChip: Chip? = null
    private val filterTagChips: MutableMap<String, Chip> = mutableMapOf()

    override fun navigateToDetails(event: Event) {
        findNavController().navigate(ConferenceEventListFragmentDirections.actionEventsListFragmentToEventDetailsFragment(eventGuid = event.guid))
    }

    override fun setupEvents(binding: FragmentEventsListBinding) {
        binding.filterFab.show()
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
            viewModel.filter.observe(viewLifecycleOwner, Observer { filter ->
                Log.d(TAG, "Current filter: $filter")
                if (filter != null) {
                    val currentFilter = viewModel.filter.value
                    if (!currentFilter?.text.isNullOrBlank()) {
                        val textChip = filterTextChip ?: Chip(requireContext()).apply {
                            isCloseIconVisible = true
                            setOnCloseIconClickListener {
                                viewModel.filterText.postValue("")
                            }
                            filterTextChip = this
                        }
                        textChip.text = "Search: ${filter.text}"
                        binding.filterChipGroup.apply {
                            if (!contains(textChip)) {
                                addView(textChip)
                            }
                        }
                    } else {
                        filterTextChip?.let {
                            binding.filterChipGroup.removeView(it)
                        }
                    }

                    filterTagChips.values.forEach { binding.filterChipGroup.removeView(it) }
                    for (tag in filter.tags) {
                        val chip = filterTagChips[tag] ?: Chip(requireContext()).apply {
                            text = "Tag: $tag"
                            isCloseIconVisible = true
                            setOnCloseIconClickListener {
                                viewModel.filterTags.postValue(
                                        viewModel.filterTags.value?.minus(tag) ?: emptySet()
                                )
                            }
                            binding.filterChipGroup.addView(this)
                            filterTagChips[tag] = this
                        }
                        if (!binding.filterChipGroup.contains(chip)) {
                            binding.filterChipGroup.addView(chip)
                        }
                    }
                }
            })

            binding.filterFab.setOnClickListener {
                val filterBottomSheet = FilterBottomSheet()
                filterBottomSheet.arguments = bundleOf(
                        "conference" to conference
                )
                filterBottomSheet.show(childFragmentManager, null)
            }
        }
        binding.swipeRefreshLayout.isEnabled = true
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.updateEventsForConference(args.conference)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        filterTextChip = null
        filterTagChips.clear()
    }

    companion object {
        private val TAG = ConferenceEventListFragment::class.java.simpleName
    }
}
