package de.nicidienase.chaosflix.touch.browse

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.ConferenceGroup
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.touch.browse.ConferencesTabBrowseFragment.OnInteractionListener
import de.nicidienase.chaosflix.touch.browse.adapters.ConferenceRecyclerViewAdapter

class ConferenceGroupFragment : BrowseFragment() {
    private var listener: OnInteractionListener? = null
    private var layoutManager: RecyclerView.LayoutManager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_conferences_page, container, false)

        val columnCount = resources.getInteger(R.integer.num_columns)
        val conferenceGroup: ConferenceGroup? = arguments?.getParcelable(ARG_GROUP)

        if (view is RecyclerView) {
            val context = view.getContext()
            layoutManager = if (columnCount <= 1) {
                LinearLayoutManager(context)
            } else {
                StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL)
            }
            view.layoutManager = layoutManager
            val conferencesAdapter: ConferenceRecyclerViewAdapter = ConferenceRecyclerViewAdapter(listener)
            conferencesAdapter.setHasStableIds(true)
            view.adapter = conferencesAdapter
            val groupId = conferenceGroup?.id
            if (groupId != null) {
                viewModel.getConferencesByGroup(groupId).observe(viewLifecycleOwner, Observer<List<Conference>> { conferenceList: List<Conference>? ->
                    if (conferenceList != null) {
                        if (conferenceList.isNotEmpty()) {
                            setLoadingOverlayVisibility(false)
                        }
                        conferencesAdapter.conferences = conferenceList
                        val layoutState = arguments?.getParcelable<Parcelable>(LAYOUTMANAGER_STATE)?.let {
                            layoutManager?.onRestoreInstanceState(it)
                        }
                    }
                })
            }
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = if (context is OnInteractionListener) {
            context
        } else {
            throw RuntimeException("$context must implement OnListFragmentInteractionListener")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(LAYOUTMANAGER_STATE, layoutManager?.onSaveInstanceState())
    }

    override fun onPause() {
        super.onPause()
        arguments?.putParcelable(LAYOUTMANAGER_STATE, layoutManager?.onSaveInstanceState())
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
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
