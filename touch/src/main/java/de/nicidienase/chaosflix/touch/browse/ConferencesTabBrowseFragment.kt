package de.nicidienase.chaosflix.touch.browse

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import de.nicidienase.chaosflix.common.mediadata.MediaRepository
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.ConferenceGroup
import de.nicidienase.chaosflix.common.viewmodel.BrowseViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.touch.browse.adapters.ConferenceGroupsFragmentPager
import de.nicidienase.chaosflix.touch.databinding.FragmentTabPagerLayoutBinding

class ConferencesTabBrowseFragment : Fragment(), SearchView.OnQueryTextListener {

    private val viewModel: BrowseViewModel by viewModels { ViewModelFactory.getInstance(requireContext()) }

    private var snackbar: Snackbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        Log.d(TAG, "onCreate")
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        findNavController().navigate(ConferencesTabBrowseFragmentDirections.actionConferencesTabBrowseFragmentToSearchFragment(query))
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentTabPagerLayoutBinding.inflate(inflater, container, false)
        val searchService = requireContext().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchService.setOnCancelListener {
            binding.search.show()
        }
        searchService.setOnDismissListener {
            binding.search.show()
        }
        binding.search.setOnClickListener {
            requireActivity().onSearchRequested()
            binding.search.hide()
        }
        viewModel.getConferenceGroups().observe(viewLifecycleOwner, Observer<List<ConferenceGroup>> { conferenceGroups ->
            val fragmentPager = ConferenceGroupsFragmentPager(requireContext(), childFragmentManager)
            fragmentPager.setContent(conferenceGroups)
            binding.viewpager.adapter = fragmentPager
            val arguments = arguments
            val viewpagerState: Parcelable?
            if (arguments != null) {
                viewpagerState = arguments.getParcelable(VIEWPAGER_STATE)
                if (viewpagerState != null) {
                    binding.viewpager.onRestoreInstanceState(viewpagerState)
                }
            }
            binding.slidingTabs.setupWithViewPager(binding.viewpager)
        })
        viewModel.getUpdateState().observe(viewLifecycleOwner, Observer { state ->
            if (state == null) {
                return@Observer
            }
            when (state.state) {
                // TODO use SwipeRefreshLayout
                MediaRepository.State.RUNNING -> {
                }
                MediaRepository.State.DONE -> {
                }
            }
            if (state.error != null) {
                showSnackbar(state.error)
            }
        })
        return binding.root
    }

    private fun showSnackbar(message: String?) {
        snackbar?.dismiss()
        if (message != null) {
            view?.let { view1 ->
                snackbar = Snackbar.make(view1, message, Snackbar.LENGTH_LONG)
                snackbar?.setAction("Okay") { snackbar?.dismiss() }
                snackbar?.show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.conferences_menu, menu)
    }

    override fun onPause() {
        super.onPause()
//        arguments?.putParcelable(VIEWPAGER_STATE, binding.viewpager.onSaveInstanceState());
    }

    companion object {
        private val TAG = ConferencesTabBrowseFragment::class.java.simpleName
        private const val VIEWPAGER_STATE = "viewpager_state"
    }
}
