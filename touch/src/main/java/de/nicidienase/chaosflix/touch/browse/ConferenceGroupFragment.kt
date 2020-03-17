package de.nicidienase.chaosflix.touch.browse

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.ConferenceGroup
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.touch.browse.adapters.ConferenceRecyclerViewAdapter

class ConferenceGroupFragment : BrowseFragment() {

	private var columnCount = 1
	private lateinit var conferenceGroup: ConferenceGroup
	private var layoutManager: RecyclerView.LayoutManager? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		if (arguments != null) {
			columnCount = arguments?.getInt(ARG_COLUMN_COUNT) ?: 1
			conferenceGroup = arguments?.getParcelable(ARG_GROUP)!!
		}
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val view = inflater.inflate(R.layout.fragment_conferences_page, container, false)
		if (view is RecyclerView) {
			val context = view.getContext()
			val recyclerView = view
			layoutManager = if (columnCount <= 1) {
				LinearLayoutManager(context)
			} else {
				GridLayoutManager(context, columnCount)
			}
			recyclerView.layoutManager = layoutManager
			val conferencesAdapter = ConferenceRecyclerViewAdapter {
				// TODO navigate to events-list for conference
			}
			conferencesAdapter?.setHasStableIds(true)
			recyclerView.adapter = conferencesAdapter
			viewModel.getConferencesByGroup(conferenceGroup.id).observe(viewLifecycleOwner, Observer<List<Conference>> { conferenceList: List<Conference>? ->
				if (conferenceList != null) {
					if (conferenceList.isNotEmpty()) {
						setLoadingOverlayVisibility(false)
					}
					conferencesAdapter.conferences = conferenceList
					val layoutState = arguments!!.getParcelable<Parcelable>(LAYOUTMANAGER_STATE)
					if (layoutState != null) {
						layoutManager!!.onRestoreInstanceState(layoutState)
					}
				}
			})
		}
		return view
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		if (layoutManager != null) {
			outState.putParcelable(LAYOUTMANAGER_STATE, layoutManager!!.onSaveInstanceState())
		}
	}

	override fun onPause() {
		super.onPause()
		if (layoutManager != null) {
			arguments!!.putParcelable(LAYOUTMANAGER_STATE, layoutManager!!.onSaveInstanceState())
		}
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