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
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import de.nicidienase.chaosflix.common.mediadata.MediaRepository
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.ConferenceGroup
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.touch.browse.adapters.ConferenceGroupsFragmentPager
import de.nicidienase.chaosflix.touch.databinding.FragmentTabPagerLayoutBinding

class ConferencesTabBrowseFragment : BrowseFragment(), SearchView.OnQueryTextListener {
	private var mColumnCount = 1
	private var binding: FragmentTabPagerLayoutBinding? = null
	private var snackbar: Snackbar? = null
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setHasOptionsMenu(true)
		Log.d(TAG, "onCreate")
		if (arguments != null) {
			mColumnCount = arguments!!.getInt(ARG_COLUMN_COUNT)
		}
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
		setupToolbar(binding.incToolbar.toolbar, R.string.app_name)
		overlay = binding.incOverlay.loadingOverlay
		binding.search.setOnClickListener {
			requireActivity().onSearchRequested()
		}
		viewModel.getConferenceGroups().observe(viewLifecycleOwner, Observer<List<ConferenceGroup?>> { conferenceGroups: List<ConferenceGroup?> ->
			if (conferenceGroups.isNotEmpty()) {
				setLoadingOverlayVisibility(false)
			}
			val fragmentPager = ConferenceGroupsFragmentPager(this.context, childFragmentManager)
			fragmentPager.setContent(conferenceGroups)
			binding.viewpager.adapter = fragmentPager
			val arguments = arguments
			var viewpagerState: Parcelable? = null
			if (arguments != null) {
				viewpagerState = arguments.getParcelable(VIEWPAGER_STATE)
				if (viewpagerState != null) {
					binding.viewpager.onRestoreInstanceState(viewpagerState)
				}
			}
			binding.slidingTabs.setupWithViewPager(binding.viewpager)
			if (conferenceGroups.isNotEmpty()) {
				setLoadingOverlayVisibility(false)
			}
		})
		viewModel.getUpdateState().observe(viewLifecycleOwner, Observer { state ->
			if (state == null) {
				return@Observer
			}
			when (state.state) {
				MediaRepository.State.RUNNING -> setLoadingOverlayVisibility(true)
				MediaRepository.State.DONE -> setLoadingOverlayVisibility(false)
			}
			if (state.error != null) {
				showSnackbar(state.error)
			}
		})
		return binding.root
	}

	private fun showSnackbar(message: String?) {
		snackbar?.dismiss()
		if(message != null){
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
		val searchMenuItem = menu.findItem(R.id.search)
		val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
		(searchMenuItem.actionView as SearchView).apply {
			setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
			isSubmitButtonEnabled = true
			isIconified = false
			setOnQueryTextListener(this@ConferencesTabBrowseFragment)
		}
	}

	override fun onPause() {
		super.onPause()
		//		getArguments().putParcelable(VIEWPAGER_STATE, binding.viewpager.onSaveInstanceState());
	}

	companion object {
		private val TAG = ConferencesTabBrowseFragment::class.java.simpleName
		private const val ARG_COLUMN_COUNT = "column-count"
		private const val CURRENTTAB_KEY = "current_tab"
		private const val VIEWPAGER_STATE = "viewpager_state"
		fun newInstance(columnCount: Int): ConferencesTabBrowseFragment {
			val fragment = ConferencesTabBrowseFragment()
			val args = Bundle()
			args.putInt(ARG_COLUMN_COUNT, columnCount)
			fragment.arguments = args
			return fragment
		}
	}
}