package de.nicidienase.chaosflix.touch.browse.eventslist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.common.viewmodel.BrowseViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.touch.databinding.FragmentFilterSheetBinding

class FilterBottomSheet : BottomSheetDialogFragment() {

    private lateinit var viewModel: BrowseViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val conference = arguments?.getParcelable<Conference>("conference")

        val binding = FragmentFilterSheetBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(
                parentFragment as ViewModelStoreOwner,
                ViewModelFactory.getInstance(requireContext()))
                .get(BrowseViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.buttonClose.setOnClickListener {
            this.dismiss()
        }
        binding.root

        conference?.let { conf ->
            viewModel.getUsefullTags(conf).observe(viewLifecycleOwner, Observer { tags ->
                binding.tagChipGroup.removeAllViews()
                tags.forEach { tag ->
                    binding.tagChipGroup.addView(Chip(requireContext()).apply {
                        text = tag
                        setOnClickListener {
                            val newTags = (viewModel.filterTags.value ?: emptySet()).plus(tag)
                            viewModel.filterTags.postValue(newTags)
                        }
                        viewModel.filter.observe(viewLifecycleOwner, Observer {
                            if(it != null){
                                this.isChecked = it.tags.contains(tag)
                            }
                        })
                    })
                }
            })
        }
        return binding.root
    }
}
