package de.nicidienase.chaosflix.leanback.conferences

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v17.leanback.app.BrowseSupportFragment
import android.support.v17.leanback.widget.ArrayObjectAdapter
import android.support.v17.leanback.widget.DividerRow
import android.support.v17.leanback.widget.HeaderItem
import android.support.v17.leanback.widget.ListRow
import android.support.v17.leanback.widget.ListRowPresenter
import android.support.v17.leanback.widget.Row
import android.support.v17.leanback.widget.SectionRow
import android.util.Log
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.ConferenceGroup
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.LiveConference
import de.nicidienase.chaosflix.common.mediadata.sync.Downloader
import de.nicidienase.chaosflix.common.util.ConferenceUtil
import de.nicidienase.chaosflix.common.viewmodel.BrowseViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.leanback.BrowseErrorFragment
import de.nicidienase.chaosflix.leanback.CardPresenter
import de.nicidienase.chaosflix.leanback.ChaosflixEventAdapter
import de.nicidienase.chaosflix.leanback.DiffCallbacks
import de.nicidienase.chaosflix.leanback.ItemViewClickedListener
import de.nicidienase.chaosflix.leanback.R

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
    private lateinit var promotedAdapter: ChaosflixEventAdapter
    private lateinit var watchListAdapter: ChaosflixEventAdapter
    private lateinit var inProgressAdapter: ChaosflixEventAdapter

    var errorFragment: BrowseErrorFragment? = null
    private lateinit var viewModel: BrowseViewModel

    val conferencePresenter = CardPresenter(R.style.ConferenceCardStyle)
    val eventPresenter = CardPresenter(R.style.EventCardStyle)

    private val conferencesGroupRows = HashMap<String, ListRow>()

    private enum class Section {
        Streaming,
        Recomendations,
        Conferences
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = resources.getString(R.string.app_name)
        badgeDrawable = resources.getDrawable(R.drawable.chaosflix_icon, null)

        viewModel = ViewModelProviders.of(this, ViewModelFactory(requireContext())).get(BrowseViewModel::class.java)

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

        viewModel.getConferenceGroups().observe(this, Observer { conferenceGroups ->
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

        viewModel.getUpdateState().observe(this, Observer { downloaderEvent ->
            when (downloaderEvent?.state) {
                Downloader.DownloaderState.RUNNING -> {
                    Log.i(TAG, "Refresh running")
                    fragmentManager?.let {
                        errorFragment = BrowseErrorFragment.showErrorFragment(it, R.id.browse_fragment)
                    }
                }
                Downloader.DownloaderState.DONE -> {
                    if (downloaderEvent.error != null) {
                        val errorMessage = downloaderEvent.error ?: "Error refreshing events"
                        errorFragment?.setErrorContent(errorMessage, fragmentManager)
                    } else {
                        errorFragment?.dismiss(fragmentManager)
                    }
                }
            }
        })

        viewModel.getBookmarkedEvents().observe(this, Observer { bookmarks ->
            if (bookmarks != null) {
                watchListAdapter.setItems(bookmarks, DiffCallbacks.eventDiffCallback)
                watchListAdapter.notifyItemRangeChanged(0, bookmarks.size)
                if (rowsAdapter.indexOf(watchlistRow) == -1) {
                    updateSectionRecomendations()
                }
            }
        })
        viewModel.getInProgressEvents().observe(this, Observer { inProgress ->
            if (inProgress != null) {
                inProgressAdapter.setItems(inProgress, DiffCallbacks.eventDiffCallback)
                inProgressAdapter.notifyItemRangeChanged(0, inProgress.size)
                if (rowsAdapter.indexOf(inProgressRow) == -1) {
                    updateSectionRecomendations()
                }
            }
        })

        viewModel.getPromotedEvents().observe(this, Observer { promoted ->
            if (promoted != null) {
                promotedAdapter.setItems(promoted, DiffCallbacks.eventDiffCallback)
                promotedAdapter.notifyItemRangeChanged(0, promoted.size)
                if (rowsAdapter.indexOf(promotedRow) == -1) {
                    updateSectionRecomendations()
                }
            }
        })

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
                    { listOf(promotedRow, watchlistRow, inProgressRow).filter { it.adapter.size() > 0 } },
                    recomendationsDivider)

    private fun updateStreams(streamRows: List<Row>) = updateSection(
            Section.Streaming,
            { streamRows },
            streamsDivider)

    private fun updateConferencesSection(rows: List<Row>) {
        if (! rows.map { rowsAdapter.indexOf(it) }.contains(-1)) {
            Log.i(TAG, "skipping conf-section update, all rows allready contained")
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
            Log.i(TAG, "skipping adding section, all rows allready contained")
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
                rowsAdapter.add(0, streamsDivider)
                rowsAdapter.add(0, streamingSection)
                rowsAdapter.notifyArrayItemRangeChanged(0, 2)
            }
            Section.Recomendations -> {
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
            rowsAdapter.notifyArrayItemRangeChanged(i + 1, j - i - 1)
        }
    }

    private fun bindConferencesToRow(group: ConferenceGroup, row: ListRow) {
        viewModel.getConferencesByGroup(group.id).observe(this, Observer { conferences ->
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
