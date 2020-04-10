package de.nicidienase.chaosflix.touch.browse

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.ConferenceGroup
import de.nicidienase.chaosflix.common.viewmodel.BrowseViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.touch.browse.adapters.ConferenceRecyclerViewAdapter
class ConferenceGroupFragment : Fragment() {

    private val viewModel: BrowseViewModel by viewModels { ViewModelFactory.getInstance(requireContext()) }

    private var layoutManager: RecyclerView.LayoutManager? = null

    private val columnCount: Int by lazy { resources.getInteger(R.integer.num_columns) }
    private val conferenceGroup: ConferenceGroup by lazy { arguments?.getParcelable<ConferenceGroup>(ARG_GROUP)!! }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_conferences_page, container, false)
        if (view is RecyclerView) {
            val context = view.getContext()
            layoutManager = if (columnCount <= 1) {
                LinearLayoutManager(context)
            } else {
                StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL)
            }
            view.layoutManager = layoutManager
            val conferencesAdapter = ConferenceRecyclerViewAdapter {
                findNavController().navigate(
                        ConferencesTabBrowseFragmentDirections.actionConferencesTabBrowseFragmentToEventsListFragment(conference = it)
                )
            }
            conferencesAdapter.setHasStableIds(true)
            view.adapter = conferencesAdapter
            viewModel.getConferencesByGroup(conferenceGroup.id).observe(viewLifecycleOwner, Observer<List<Conference>> { conferenceList: List<Conference>? ->
                if (conferenceList != null) {
                    conferencesAdapter.conferences = conferenceList
                    arguments?.getParcelable<Parcelable>(LAYOUTMANAGER_STATE)?.let {
                        layoutManager?.onRestoreInstanceState(it)
                    }
                }
            })
        }
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        layoutManager?.let {
            outState.putParcelable(LAYOUTMANAGER_STATE, it.onSaveInstanceState())
        }
    }

    override fun onPause() {
        super.onPause()
        layoutManager?.let {
            arguments?.putParcelable(LAYOUTMANAGER_STATE, it.onSaveInstanceState())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        layoutManager = null
    }

    companion object {
        private val TAG = ConferenceGroupFragment::class.java.simpleName
        private const val ARG_COLUMN_COUNT = "column-count"
        private const val ARG_GROUP = "group-name"
        private const val LAYOUTMANAGER_STATE = "layoutmanager-state"
        @JvmStatic
        fun newInstance(group: ConferenceGroup?, columnCount: Int): ConferenceGroupFragment {
            val fragment = ConferenceGroupFragment()
            val args = Bundle()
            args.putInt(ARG_COLUMN_COUNT, columnCount)
            args.putParcelable(ARG_GROUP, group)
            fragment.arguments = args
            return fragment
        }
    }
}

