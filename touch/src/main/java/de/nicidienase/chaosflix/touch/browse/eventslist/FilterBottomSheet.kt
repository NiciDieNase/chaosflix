package de.nicidienase.chaosflix.touch.browse.eventslist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.contains
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.common.viewmodel.BrowseViewModel
import de.nicidienase.chaosflix.touch.databinding.FragmentFilterSheetBinding
import org.koin.android.viewmodel.ext.android.viewModel

class FilterBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: BrowseViewModel by viewModel()

    private val filterTagChips: MutableMap<String, Chip> = mutableMapOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val conference = arguments?.getParcelable<Conference>("conference")

        val binding = FragmentFilterSheetBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.buttonClose.setOnClickListener {
            this.dismiss()
        }
        binding.root

        conference?.let { conf ->
            viewModel.getUsefullTags(conf).observe(viewLifecycleOwner, Observer { tags ->
                filterTagChips.forEach {
                    if (!tags.contains(it.key)) {
                        binding.tagChipGroup.removeView(it.value)
                        filterTagChips.remove(it.key)
                    }
                }
                tags.forEach { tag ->
                    val chip: Chip = filterTagChips[tag] ?: Chip(requireContext()).apply {
                        text = tag
                        isCheckable = true
                        setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) {
                                val newTags = (viewModel.filterTags.value ?: emptySet()).plus(tag)
                                viewModel.filterTags.postValue(newTags)
                            } else {
                                val newTags = viewModel.filterTags.value?.minus(tag) ?: emptySet()
                                viewModel.filterTags.postValue(newTags)
                            }
                        }
                        filterTagChips[tag] = this
                    }
                    if (!binding.tagChipGroup.contains(chip)) {
                        binding.tagChipGroup.addView(chip)
                    }
                }
            })
        }
        viewModel.filter.observe(viewLifecycleOwner, Observer {
            it.tags.forEach {
                filterTagChips[it]?.isChecked = true
            }
        })
        return binding.root
    }
}
