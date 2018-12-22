package de.nicidienase.chaosflix.leanback.conferences

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v17.leanback.app.BrowseSupportFragment
import android.support.v17.leanback.widget.*
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.ConferenceGroup
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.LiveConference
import de.nicidienase.chaosflix.common.mediadata.sync.Downloader
import de.nicidienase.chaosflix.common.util.ConferenceUtil
import de.nicidienase.chaosflix.common.viewmodel.BrowseViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.leanback.CardPresenter
import de.nicidienase.chaosflix.leanback.ChaosflixEventAdapter
import de.nicidienase.chaosflix.leanback.ItemViewClickedListener
import de.nicidienase.chaosflix.leanback.R
import de.nicidienase.chaosflix.leanback.BrowseErrorFragment
import java.util.*

class ConferencesBrowseFragment : BrowseSupportFragment() {
	private val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
	private lateinit var streamingSection: SectionRow
	private lateinit var recomendationsSections: SectionRow
	private lateinit var conferencesSection: SectionRow
	private lateinit var streamsDivider: DividerRow
	private lateinit var recomendationsDivider: DividerRow
//	private lateinit var promotedRow: ListRow
	private lateinit var watchlistRow: ListRow
	private lateinit var inProgressRow: ListRow
//	private lateinit var promotedAdapter: ChaosflixEventAdapter
	private lateinit var watchListAdapter: ChaosflixEventAdapter
	private lateinit var inProgressAdapter: ChaosflixEventAdapter


	private lateinit var viewModel: BrowseViewModel

	private val conferencesGroupRows = HashMap<String, ListRow>()
	private val eventDiffCallback = object : DiffCallback<Event>() {
		override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
			return oldItem.guid == newItem.guid
		}

		override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
			return oldItem.guid == newItem.guid
		}
	}

	private enum class Section {
		Streaming,
		Recomendations,
		Conferences
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		title = resources.getString(R.string.app_name)
		badgeDrawable = resources.getDrawable(R.drawable.chaosflix_icon)

		//		setHeaderPresenterSelector(new PresenterSelector() {
		//			@Override
		//			public Presenter getPresenter(Object item) {
		//				return new HeaderItemPresenter();
		//			}
		//		});

		viewModel = ViewModelProviders.of(this, ViewModelFactory(requireContext())).get(BrowseViewModel::class.java)

		val fragmentManager = fragmentManager
		val errorFragment: BrowseErrorFragment?
		errorFragment = if (fragmentManager != null) {
			BrowseErrorFragment.showErrorFragment(fragmentManager, FRAGMENT)
		} else {
			null
		}
		val conferencePresenter = CardPresenter(R.style.ConferenceCardStyle)
		val eventPresenter = CardPresenter(R.style.EventCardStyle)


		// Recomendation Rows and Adapter
		watchListAdapter = ChaosflixEventAdapter(eventPresenter)
		inProgressAdapter = ChaosflixEventAdapter(eventPresenter)
//		promotedAdapter = ChaosflixEventAdapter(eventPresenter)
//		promotedRow = ListRow(HeaderItem("Promoted"), promotedAdapter)
		watchlistRow = ListRow(HeaderItem(getString(R.string.watchlist)), watchListAdapter)
		inProgressRow = ListRow(HeaderItem("Continue Watching"), inProgressAdapter)

		// Sections and Divider
		streamingSection = SectionRow(HeaderItem(getString(R.string.livestreams)))
		streamsDivider = DividerRow()
		recomendationsSections = SectionRow(HeaderItem(getString(R.string.recomendations)))
		recomendationsDivider = DividerRow()
		conferencesSection = SectionRow(HeaderItem(getString(R.string.conferences)))

		rowsAdapter.add(conferencesSection)

		onItemViewClickedListener = ItemViewClickedListener(this)
		adapter = rowsAdapter

		viewModel.getUpdateState().observe(this, Observer { downloaderEvent ->
			downloaderEvent?.error?.let {
				if (errorFragment != null && !errorFragment.isDetached) {
					val errorMessage = downloaderEvent.error ?: "Error Refreshing Events"
					errorFragment.setErrorContent(errorMessage)
				}
			}
			when (downloaderEvent?.state) {
				Downloader.DownloaderState.RUNNING -> {
				}
				Downloader.DownloaderState.DONE -> errorFragment?.dismiss()
			}
		})

		viewModel.getConferenceGroups().observe(this, Observer { conferenceGroups ->
			if (conferenceGroups != null && conferenceGroups.isNotEmpty()) {
				val conferenceRows = ArrayList<Row>()
				errorFragment?.dismiss()
				for (group in conferenceGroups.sorted()) {
					val row: ListRow

					if (conferencesGroupRows.containsKey(group.name)) {
						row = conferencesGroupRows[group.name]!!
					} else {
						row = buildRow(ArrayList(), conferencePresenter, group.name, ConferenceUtil.getStringForTag(group.name))
						conferenceRows.add(row)
						conferencesGroupRows[group.name] = row
					}
					bindConferencesToRow(group, row)
				}
				updateConferencesSection(conferenceRows)
			}
		})

		viewModel.getBookmarkedEvents().observe(this, Observer { bookmarks ->
			if (bookmarks != null) {
				watchListAdapter.setItems(bookmarks, eventDiffCallback)
				watchListAdapter.notifyItemRangeChanged(0, bookmarks.size)
				if(rowsAdapter.indexOf(watchlistRow) == -1){
					updateSectionRecomendations()
				}
			}
		})
		viewModel.getInProgressEvents().observe(this, Observer { inProgress ->
			if (inProgress != null) {
				inProgressAdapter.setItems(inProgress, eventDiffCallback)
				inProgressAdapter.notifyItemRangeChanged(0, inProgress.size)
				if(rowsAdapter.indexOf(inProgressRow) == -1){
					updateSectionRecomendations()
				}
			}
		})

//		viewModel.getPromotedEvents().observe(this, Observer { promoted ->
//			if (promoted != null) {
//				promotedAdapter.setItems(promoted, eventDiffCallback)
//				promotedAdapter.notifyItemRangeChanged(0, promoted.size)
//				if(rowsAdapter.indexOf(promotedRow) == -1 ){
//					updateSectionRecomendations()
//				}
//			}
//		})

		viewModel.getLivestreams().observe(this, Observer { liveConferences ->
			if (liveConferences != null && liveConferences.isNotEmpty()) {
				val streamRows = buildStreamRows(eventPresenter, liveConferences)
				updateStreams(streamRows)
			}
		})
	}

	private fun updateSectionRecomendations() =
			updateSection(
					Section.Recomendations,
//					{ listOf(promotedRow, watchlistRow, inProgressRow).filter { it.adapter.size() > 0 } },
					{ listOf(watchlistRow, inProgressRow).filter { it.adapter.size() > 0 } },
					recomendationsDivider)

	private fun updateStreams(streamRows: List<Row>) = updateSection(
			Section.Streaming,
			{ streamRows },
			streamsDivider)

	private fun updateConferencesSection(rows: List<Row>) {
		clearSection(Section.Conferences)
		rowsAdapter.addAll(rowsAdapter.size(), rows)
	}

	private fun updateSection(section: Section, rowProvider: () -> List<Row>, before: Row) {
		val rows = rowProvider.invoke()
		if (rows.isNotEmpty()) {
			if (sectionVisible(section)) {
				clearSection(section)
			} else {
				addSectionIfNecessary(section)
			}
			rowsAdapter.addAll(rowsAdapter.indexOf(before), rows)
		} else {
			if (sectionVisible(section)) {
				removeSection(section)
			}
		}
	}

	private fun addSectionIfNecessary(section: Section) {
		when (section) {
			Section.Streaming -> {
				rowsAdapter.add(0, streamsDivider)
				rowsAdapter.add(0, streamingSection)
			}
			Section.Recomendations -> {
				val index = rowsAdapter.indexOf(conferencesSection)
				rowsAdapter.add(index, recomendationsDivider)
				rowsAdapter.add(index, recomendationsSections)
			}
			Section.Conferences -> {
				rowsAdapter.add(rowsAdapter.size(), conferencesSection)
			}
		}
	}

	private fun removeSection(section: Section) {
		if (!sectionVisible(section)) {
			return
		}
		val pair = getSectionIndices(section)
		rowsAdapter.removeItems(pair.first, pair.second - pair.first + 1)
	}

	private fun getSectionIndices(section: Section): Pair<Int, Int> {
		return when (section) {
			Section.Streaming -> {
				Pair(rowsAdapter.indexOf(streamingSection), rowsAdapter.indexOf(streamsDivider))
			}
			Section.Recomendations -> {
				Pair(rowsAdapter.indexOf(recomendationsSections), rowsAdapter.indexOf(recomendationsDivider))
			}
			Section.Conferences -> {
				Pair(rowsAdapter.indexOf(conferencesSection), rowsAdapter.size() - 1)
			}
		}
	}

	private fun sectionVisible(section: Section): Boolean {
		val index = when (section) {
			Section.Streaming -> rowsAdapter.indexOf(streamingSection)
			Section.Recomendations -> rowsAdapter.indexOf(recomendationsSections)
			Section.Conferences -> rowsAdapter.indexOf(conferencesSection)
		}
		return index != -1
	}

	private fun clearSection(section: Section) {
		val (i, j) = getSectionIndices(section)
		if (i + 1 < rowsAdapter.size() - 1) {
			rowsAdapter.removeItems(i + 1, j - i - 1)
		}
	}

	private fun bindConferencesToRow(group: ConferenceGroup, row: ListRow) {
		viewModel.getConferencesByGroup(group.id).observe(this, Observer { conferences ->
			(row.adapter as ArrayObjectAdapter).setItems(conferences?.sorted(), object : DiffCallback<Conference>() {
				override fun areItemsTheSame(oldItem: Conference, newItem: Conference): Boolean {
					return oldItem.url == newItem.url
				}

				override fun areContentsTheSame(oldItem: Conference, newItem: Conference): Boolean {
					return oldItem.updatedAt == newItem.updatedAt
				}
			})
		})
	}

	private fun buildStreamRows(cardPresenter: CardPresenter, liveConferences: List<LiveConference>): List<Row> {
		val rows = ArrayList<Row>()
		if (liveConferences.isNotEmpty()) {
			for (con in liveConferences) {
//				rows.add(SectionRow(HeaderItem(con.conference)))
				for ((group, rooms) in con.groups) {
					// setup header
					val groupName = if (group.isNotEmpty()) "${con.conference} - $group" else con.conference
					val header = HeaderItem(groupName)
					header.description = "${con.conference} - ${con.description}"
					header.contentDescription = groupName
					// setup list
					val listRowAdapter = ArrayObjectAdapter(cardPresenter)
					listRowAdapter.addAll(listRowAdapter.size(), rooms)
					rows.add(ListRow(header, listRowAdapter))
				}
			}
		}
		return rows
	}

	private fun buildRow(conferences: List<Conference>, cardPresenter: CardPresenter, tag: String, description: String): ListRow {
		val listRowAdapter = ArrayObjectAdapter(cardPresenter)
		listRowAdapter.addAll(0, conferences)
		val header = HeaderItem(ConferenceUtil.getStringForTag(tag))
		//		header.setDescription(description);
		return ListRow(header, listRowAdapter)
	}

	companion object {
		const val FRAGMENT = R.id.browse_fragment
		private val TAG = ConferencesBrowseFragment::class.java.simpleName
	}
}
