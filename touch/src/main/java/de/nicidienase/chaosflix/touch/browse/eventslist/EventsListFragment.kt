package de.nicidienase.chaosflix.touch.browse.eventslist

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.viewmodel.BrowseViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.touch.browse.adapters.EventRecyclerViewAdapter
import de.nicidienase.chaosflix.touch.databinding.FragmentEventsListBinding

abstract class EventsListFragment : Fragment() {

    protected val viewModel: BrowseViewModel by viewModels { ViewModelFactory.getInstance(requireContext()) }

    private var columnCount = 1
    protected lateinit var eventAdapter: EventRecyclerViewAdapter
    private var layoutManager: LinearLayoutManager? = null
    private var snackbar: Snackbar? = null
    private var type = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        columnCount = resources.getInteger(R.integer.num_columns)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentEventsListBinding.inflate(inflater, container, false)
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.incToolbar.toolbar)
        layoutManager = if (columnCount <= 1) {
            LinearLayoutManager(context)
        } else {
            GridLayoutManager(context, columnCount)
        }
        binding.list.layoutManager = layoutManager
        eventAdapter = EventRecyclerViewAdapter { navigateToDetails(it) }
        eventAdapter.setHasStableIds(true)
        binding.list.adapter = eventAdapter
//        layoutManager?.let {
//            val itemDecoration = DividerItemDecoration(binding.list.context, it.orientation)
//            binding.list.addItemDecoration(itemDecoration)
//        }
        binding.swipeRefreshLayout.isEnabled = false
        binding.filterFab.hide()
        setupEvents(binding)
        return binding.root
    }

    protected abstract fun navigateToDetails(event: Event)

    protected abstract fun setupEvents(binding: FragmentEventsListBinding)

    protected fun setRefreshing(binding: FragmentEventsListBinding, refreshing: Boolean) {
        binding.swipeRefreshLayout.isRefreshing = refreshing
    }

    protected fun showSnackbar(message: String, binding: FragmentEventsListBinding) {
        snackbar?.dismiss()
        snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).apply {
            setAction("Okay") { view: View? -> snackbar?.dismiss() }
            show()
        }
    }

    protected fun setEvents(events: List<Event>) {
        eventAdapter?.submitList(events)
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
    }

    companion object {
        private const val ARG_TYPE = "type"
        private const val ARG_CONFERENCE = "conference"
        private const val LAYOUTMANAGER_STATE = "layoutmanager-state"
        private val TAG = EventsListFragment::class.java.simpleName
        const val TYPE_EVENTS = 0
        const val TYPE_BOOKMARKS = 1
        const val TYPE_IN_PROGRESS = 2
        fun newInstance(type: Int, conference: Conference?): EventsListFragment {
            val fragment = ConferenceEventListFragment()
            val args = Bundle()
            args.putInt(ARG_TYPE, type)
            args.putParcelable(ARG_CONFERENCE, conference)
            fragment.arguments = args
            return fragment
        }
    }
}
