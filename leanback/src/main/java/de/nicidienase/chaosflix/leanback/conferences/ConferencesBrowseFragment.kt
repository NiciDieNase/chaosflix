package de.nicidienase.chaosflix.leanback.conferences

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.DividerRow
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.SectionRow
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.nicidienase.chaosflix.common.mediadata.MediaRepository
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.ConferenceGroup
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.LiveConference
import de.nicidienase.chaosflix.common.util.ConferenceUtil
import de.nicidienase.chaosflix.common.viewmodel.BrowseViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.leanback.BrowseErrorFragment
import de.nicidienase.chaosflix.leanback.BuildConfig
import de.nicidienase.chaosflix.leanback.CardPresenter
import de.nicidienase.chaosflix.leanback.ChaosflixEventAdapter
import de.nicidienase.chaosflix.leanback.DiffCallbacks
import de.nicidienase.chaosflix.leanback.ItemViewClickedListener
import de.nicidienase.chaosflix.leanback.R
import de.nicidienase.chaosflix.leanback.SelectableContentItem

class ConferencesBrowseFragment : BrowseSupportFragment() {
    private val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
    private lateinit var streamingSection: SectionRow
    private lateinit var recomendationsSections: SectionRow
    private lateinit var conferencesSection: SectionRow
    private lateinit var streamsDivider: DividerRow
    private lateinit var recomendationsDivider: DividerRow
    private lateinit var promotedRow: ListRow
    private lateinit var watchlistRow: ListRow
    private lateinit var inProgressRow: ListRow
    private lateinit var settingsRow: ListRow
    private lateinit var promotedAdapter: ChaosflixEventAdapter
    private lateinit var watchListAdapter: ChaosflixEventAdapter
    private lateinit var inProgressAdapter: ChaosflixEventAdapter

    var errorFragment: BrowseErrorFragment? = null
    private lateinit var viewModel: BrowseViewModel

    private val conferencePresenter = CardPresenter(R.style.ConferenceCardStyle)
    private val eventPresenter = CardPresenter(R.style.EventCardStyle)
    private val settingsPresenter = CardPresenter(R.style.SettingsCardStyle)

    private val conferencesGroupRows = HashMap<String, ListRow>()

    private enum class Section {
        Streaming,
        Recommendations,
        Conferences
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = resources.getString(R.string.app_name)
        badgeDrawable = resources.getDrawable(R.drawable.chaosflix_icon, null)

        viewModel = ViewModelProviders.of(this, ViewModelFactory.getInstance(requireContext())).get(BrowseViewModel::class.java)

        // Recomendation Rows and Adapter
        watchListAdapter = ChaosflixEventAdapter(eventPresenter)
        inProgressAdapter = ChaosflixEventAdapter(eventPresenter)
        promotedAdapter = ChaosflixEventAdapter(eventPresenter)
        promotedRow = ListRow(HeaderItem(getString(R.string.recomended)), promotedAdapter)
        watchlistRow = ListRow(HeaderItem(getString(R.string.watchlist)), watchListAdapter)
        inProgressRow = ListRow(HeaderItem(getString(R.string.continue_watching)), inProgressAdapter)

        // Sections and Divider
        streamingSection = SectionRow(HeaderItem(getString(R.string.livestreams)))
        streamsDivider = DividerRow()
        recomendationsSections = SectionRow(HeaderItem(getString(R.string.recomendations)))
        recomendationsDivider = DividerRow()
        conferencesSection = SectionRow(HeaderItem(getString(R.string.conferences)))

        rowsAdapter.add(conferencesSection)

        onItemViewClickedListener = ItemViewClickedListener(this)
        adapter = rowsAdapter

        val listRowAdapter = ArrayObjectAdapter(settingsPresenter)
        listRowAdapter.add(SelectableContentItem.Settings)
        listRowAdapter.add(SelectableContentItem.About)
        if (BuildConfig.DEBUG) {
            listRowAdapter.add(SelectableContentItem.LeakCanary)
        }
        settingsRow = ListRow(HeaderItem("Chaosflix"), listRowAdapter)
        rowsAdapter.add(0, settingsRow)

        viewModel.getConferenceGroups().observe(viewLifecycleOwner, Observer { conferenceGroups ->
            if (conferenceGroups != null && conferenceGroups.isNotEmpty()) {
                val conferenceRows = ArrayList<Row>()
                errorFragment?.dismiss(fragmentManager)
                for (group in conferenceGroups.sorted()) {
                    var row = conferencesGroupRows.get(group.name)
                    if (row == null) {
                        row = buildRow(ArrayList(), conferencePresenter, ConferenceUtil.getStringForTag(group.name))
                        conferencesGroupRows[group.name] = row
                        bindConferencesToRow(group, row)
                    }
                    conferenceRows.add(row)
                }
                Log.i(TAG, "got ${conferenceGroups.size} conference-groups, loading ${conferenceRows.size} rows")
                updateConferencesSection(conferenceRows)
            }
        })

        viewModel.getUpdateState().observe(viewLifecycleOwner, Observer { downloaderEvent ->
            when (downloaderEvent?.state) {
                MediaRepository.State.RUNNING -> {
                    Log.i(TAG, "Refresh running")
                    fragmentManager?.let {
                        errorFragment = BrowseErrorFragment.showErrorFragment(it, R.id.browse_fragment)
                    }
                }
                MediaRepository.State.DONE -> {
                    if (downloaderEvent.error != null) {
                        val errorMessage = downloaderEvent.error ?: "Error refreshing events"
                        errorFragment?.setErrorContent(errorMessage, fragmentManager)
                    } else {
                        errorFragment?.dismiss(fragmentManager)
                    }
                }
            }
        })

        viewModel.getBookmarkedEvents().observe(viewLifecycleOwner, Observer { bookmarks ->
            if (bookmarks != null) {
                watchListAdapter.setItems(bookmarks, DiffCallbacks.eventDiffCallback)
                watchListAdapter.notifyItemRangeChanged(0, bookmarks.size)
                if (rowsAdapter.indexOf(watchlistRow) == -1) {
                    updateSectionRecomendations()
                }
            }
        })
        viewModel.getInProgressEvents().observe(viewLifecycleOwner, Observer { inProgress ->
            if (inProgress != null) {
                inProgressAdapter.setItems(inProgress, DiffCallbacks.eventDiffCallback)
                inProgressAdapter.notifyItemRangeChanged(0, inProgress.size)
                if (rowsAdapter.indexOf(inProgressRow) == -1) {
                    updateSectionRecomendations()
                }
            }
        })

        viewModel.getPromotedEvents().observe(viewLifecycleOwner, Observer { promoted ->
            if (promoted != null) {
                promotedAdapter.setItems(promoted, DiffCallbacks.eventDiffCallback)
                promotedAdapter.notifyItemRangeChanged(0, promoted.size)
                if (rowsAdapter.indexOf(promotedRow) == -1) {
                    updateSectionRecomendations()
                }
            }
        })

        viewModel.getLivestreams().observe(viewLifecycleOwner, Observer { liveConferences ->
            if (liveConferences != null && liveConferences.isNotEmpty()) {
                val streamRows = buildStreamRows(eventPresenter, liveConferences)
                updateStreams(streamRows)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        errorFragment = null
    }

    private fun updateSectionRecomendations() =
            updateSection(
                    Section.Recommendations,
                    { listOf(
                        promotedRow,
                        watchlistRow // ,
//                        inProgressRow
                    ).filter { it.adapter.size() > 0 } },
                    recomendationsDivider)

    private fun updateStreams(streamRows: List<Row>) = updateSection(
            Section.Streaming,
            { streamRows },
            streamsDivider)

    private fun updateConferencesSection(rows: List<Row>) {
        if (! rows.map { rowsAdapter.indexOf(it) }.contains(-1)) {
            Log.i(TAG, "skipping conf-section update, all rows already contained")
            return
        }
        clearSection(Section.Conferences)
        val i = rowsAdapter.indexOf(conferencesSection)
        rowsAdapter.addAll(i + 1, rows)
        rowsAdapter.notifyArrayItemRangeChanged(i, rows.size)
    }

    private fun updateSection(section: Section, rowProvider: () -> List<Row>, before: Row) {
        val rows = rowProvider.invoke()
        if (!rows.map { rowsAdapter.indexOf(it) }.contains(-1)) {
            Log.i(TAG, "skipping adding section, all rows already contained")
            return
        }
        if (rows.isNotEmpty()) {
            if (sectionVisible(section)) {
                clearSection(section)
            } else {
                    addSectionIfNecessary(section)
            }
            val i = rowsAdapter.indexOf(before)
            rowsAdapter.addAll(i, rows)
            rowsAdapter.notifyArrayItemRangeChanged(i, rows.size)
        } else {
            if (sectionVisible(section)) {
                removeSection(section)
            }
        }
    }

    private fun addSectionIfNecessary(section: Section) {
        when (section) {
            Section.Streaming -> {
                val startIndex = 1
                rowsAdapter.add(startIndex, streamsDivider)
                rowsAdapter.add(startIndex, streamingSection)
                rowsAdapter.notifyArrayItemRangeChanged(startIndex, 2)
            }
            Section.Recommendations -> {
                val index = rowsAdapter.indexOf(conferencesSection)
                rowsAdapter.add(index, recomendationsDivider)
                rowsAdapter.add(index, recomendationsSections)
                rowsAdapter.notifyArrayItemRangeChanged(index, 2)
            }
            Section.Conferences -> {
                val i = rowsAdapter.size()
                rowsAdapter.add(i, conferencesSection)
                rowsAdapter.notifyArrayItemRangeChanged(i, 1)
            }
        }
    }

    private fun removeSection(section: Section) {
        if (!sectionVisible(section)) {
            return
        }
        val pair = getSectionIndices(section)
        rowsAdapter.removeItems(pair.first, pair.second - pair.first + 1)
        rowsAdapter.notifyArrayItemRangeChanged(pair.first, pair.second - pair.first + 1)
    }

    private fun getSectionIndices(section: Section): Pair<Int, Int> {
        return when (section) {
            Section.Streaming -> {
                Pair(rowsAdapter.indexOf(streamingSection), rowsAdapter.indexOf(streamsDivider))
            }
            Section.Recommendations -> {
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
            Section.Recommendations -> rowsAdapter.indexOf(recomendationsSections)
            Section.Conferences -> rowsAdapter.indexOf(conferencesSection)
        }
        return index != -1
    }

    private fun clearSection(section: Section) {
        val (i, j) = getSectionIndices(section)
        if (i + 1 < rowsAdapter.size() - 1) {
            rowsAdapter.removeItems(i + 1, j - i - 1)
            rowsAdapter.notifyArrayItemRangeChanged(i + 1, j - i - 1)
        }
    }

    private fun bindConferencesToRow(group: ConferenceGroup, row: ListRow) {
        viewModel.getConferencesByGroup(group.id).observe(viewLifecycleOwner, Observer { conferences ->
            (row.adapter as ArrayObjectAdapter).setItems(conferences?.sorted(), DiffCallbacks.conferenceDiffCallback)
        })
    }

    private fun buildStreamRows(cardPresenter: CardPresenter, liveConferences: List<LiveConference>): List<Row> {
        val rows = ArrayList<Row>()
        if (liveConferences.isNotEmpty()) {
            for (con in liveConferences) {
// 				rows.add(SectionRow(HeaderItem(con.conference)))
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

    private fun buildRow(conferences: List<Conference>, cardPresenter: CardPresenter, title: String, description: String? = null): ListRow {
        val listRowAdapter = ArrayObjectAdapter(cardPresenter)
        listRowAdapter.addAll(0, conferences)
        val header = HeaderItem(title)
        description?.let {
            header.setDescription(it)
        }
        return ListRow(header, listRowAdapter)
    }

    companion object {
        const val FRAGMENT = R.id.browse_fragment
        private val TAG = ConferencesBrowseFragment::class.java.simpleName
    }
}
