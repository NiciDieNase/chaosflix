package de.nicidienase.chaosflix.touch.browse.eventslist

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import de.nicidienase.chaosflix.common.mediadata.MediaRepository
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.touch.browse.BrowseFragment
import de.nicidienase.chaosflix.touch.browse.adapters.EventRecyclerViewAdapter
import de.nicidienase.chaosflix.touch.databinding.FragmentEventsListBinding

class EventsListFragment : BrowseFragment(), SearchView.OnQueryTextListener {
    private var columnCount = 1
    private var eventAdapter: EventRecyclerViewAdapter? = null
    private var conference: Conference? = null
    private var layoutManager: LinearLayoutManager? = null
    private var snackbar: Snackbar? = null
    private var type = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            columnCount = arguments?.getInt(ARG_COLUMN_COUNT) ?: 1
            type = arguments?.getInt(ARG_TYPE) ?: TYPE_EVENTS
            conference = arguments?.getParcelable(ARG_CONFERENCE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentEventsListBinding.inflate(inflater, container, false)
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.incToolbar.toolbar)
        overlay = binding.incOverlay.loadingOverlay
        layoutManager = if (columnCount <= 1) {
            LinearLayoutManager(context)
        } else {
            GridLayoutManager(context, columnCount)
        }
        binding.list.layoutManager = layoutManager
        eventAdapter = EventRecyclerViewAdapter { event ->
            findNavController().navigate(EventsListFragmentDirections.actionEventsListFragmentToEventDetailsFragment(event))
        }
        eventAdapter?.setHasStableIds(true)
        binding.list.adapter = eventAdapter
        layoutManager?.let {
            val itemDecoration = DividerItemDecoration(binding.list.context, it.orientation)
            binding.list.addItemDecoration(itemDecoration)
        }
        val listObserver = Observer<List<Event>> { persistentEvents: List<Event>? ->
            setLoadingOverlayVisibility(false)
            persistentEvents?.let { setEvents(it) }
        }
        if (type == TYPE_BOOKMARKS) {
            setupToolbar(binding.incToolbar.toolbar, R.string.bookmarks)
            viewModel.getBookmarkedEvents().observe(viewLifecycleOwner, listObserver)
        } else if (type == TYPE_IN_PROGRESS) {
            setupToolbar(binding.incToolbar.toolbar, R.string.continue_watching)
            viewModel.getInProgressEvents().observe(viewLifecycleOwner, listObserver)
        } else if (type == TYPE_EVENTS) {
            run {
                setupToolbar(binding.incToolbar.toolbar, conference?.title ?: "", false)
                // 				eventAdapter.setShowTags(conference.getTagsUsefull());
                viewModel.getEventsforConference(conference!!).observe(viewLifecycleOwner, Observer { events: List<Event>? ->
                    if (events != null) {
                        setEvents(events)
                        setLoadingOverlayVisibility(false)
                    }
                })
                viewModel.updateEventsForConference(conference!!).observe(viewLifecycleOwner, Observer { state ->
                    when (state.state) {
                        MediaRepository.State.RUNNING -> setLoadingOverlayVisibility(true)
                        MediaRepository.State.DONE -> setLoadingOverlayVisibility(false)
                    }
                    if (state.error != null) {
                        showSnackbar(state.error)
                    }
                })
            }
        }
        return binding.root
    }

    private fun showSnackbar(message: String?) {
        if (snackbar != null) {
            snackbar?.dismiss()
        }
        snackbar = Snackbar.make(view!!, message!!, Snackbar.LENGTH_LONG)
        snackbar?.setAction("Okay") { view: View? -> snackbar?.dismiss() }
        snackbar?.show()
    }

    private fun setEvents(events: List<Event>) {
        eventAdapter?.items = events
        val layoutState = arguments?.getParcelable<Parcelable>(LAYOUTMANAGER_STATE)
        if (layoutState != null) {
            layoutManager?.onRestoreInstanceState(layoutState)
        }
    }

    override fun onPause() {
        super.onPause()
        arguments?.putParcelable(LAYOUTMANAGER_STATE, layoutManager?.onSaveInstanceState())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.events_menu, menu)
        val searchMenuItem = menu.findItem(R.id.search)
        val searchView = searchMenuItem.actionView as SearchView
        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
        searchView.isSubmitButtonEnabled = true
        searchView.isIconified = false
        searchView.setOnQueryTextListener(this)
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        eventAdapter?.filter?.filter(newText)
        return true
    }

    companion object {
        private const val ARG_COLUMN_COUNT = "column-count"
        private const val ARG_TYPE = "type"
        private const val ARG_CONFERENCE = "conference"
        private const val LAYOUTMANAGER_STATE = "layoutmanager-state"
        private val TAG = EventsListFragment::class.java.simpleName
        const val TYPE_EVENTS = 0
        const val TYPE_BOOKMARKS = 1
        const val TYPE_IN_PROGRESS = 2
        fun newInstance(type: Int, conference: Conference?, columnCount: Int): EventsListFragment {
            val fragment = EventsListFragment()
            val args = Bundle()
            args.putInt(ARG_TYPE, type)
            args.putInt(ARG_COLUMN_COUNT, columnCount)
            args.putParcelable(ARG_CONFERENCE, conference)
            fragment.arguments = args
            return fragment
        }
    }
}
