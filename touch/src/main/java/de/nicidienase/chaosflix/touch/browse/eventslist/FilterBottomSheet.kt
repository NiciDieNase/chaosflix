package de.nicidienase.chaosflix.touch.browse.eventslist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.nicidienase.chaosflix.common.viewmodel.BrowseViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.touch.databinding.FragmentFilterSheetBinding

class FilterBottomSheet : BottomSheetDialogFragment() {

    private lateinit var viewModel: BrowseViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentFilterSheetBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(
                parentFragment as ViewModelStoreOwner,
                ViewModelFactory.getInstance(requireContext()))
                .get(BrowseViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }
}
