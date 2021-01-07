package de.nicidienase.chaosflix.touch.eventdetails

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import de.nicidienase.chaosflix.common.ChaosflixUtil
import de.nicidienase.chaosflix.common.OfflineItemManager
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import de.nicidienase.chaosflix.common.userdata.entities.watchlist.WatchlistItem
import de.nicidienase.chaosflix.common.viewmodel.DetailsViewModel
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.touch.browse.adapters.EventRecyclerViewAdapter
import de.nicidienase.chaosflix.touch.databinding.FragmentEventDetailsBinding
import de.nicidienase.chaosflix.touch.playback.PlaybackItem
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel
import kotlin.math.abs

class EventDetailsFragment : Fragment() {

    private var appBarExpanded: Boolean = false
    private var watchlistItem: WatchlistItem? = null

    private val detailsViewModel: DetailsViewModel by viewModel()
    private var selectDialog: AlertDialog? = null
    private var pendingDownload: Pair<Event, Recording>? = null

    private lateinit var relatedEventsAdapter: EventRecyclerViewAdapter

    private var layout: View? = null

    private val args: EventDetailsFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
//        postponeEnterTransition()
//        val transition = TransitionInflater.from(context)
//                .inflateTransition(android.R.transition.move)
//        		transition.setDuration(getResources().getInteger(R.integer.anim_duration));
//        sharedElementEnterTransition = transition
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = inflater.inflate(R.layout.fragment_event_details, container, false)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentEventDetailsBinding.bind(view)
        binding.playFab.setOnClickListener { play() }
        (activity as AppCompatActivity).setSupportActionBar(binding.animToolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val eventGuid = args.eventGuid
        val eventName = args.eventName
        Log.d(TAG, "View created for Event $eventName ($eventGuid)")
        lifecycleScope.launch {
            val eventLivedata = when {
                eventGuid != null -> detailsViewModel.setEventByGuid(eventGuid)
                eventName != null -> detailsViewModel.setEventFromLink(eventName)
                else -> error("neither guid nor event-name set")
            }
            eventLivedata.observe(viewLifecycleOwner, Observer { event ->
                if (event != null) {
                    Log.d(TAG, "Update Event")
                    binding.event = event
                    binding.lifecycleOwner = viewLifecycleOwner
                    Log.d(TAG, "Loading Event ${event.title}, ${event.guid}")
                    binding.thumbImage.transitionName = getString(R.string.thumbnail) + event.guid

                    binding.tagChipGroup.removeAllViews()
                    val chips = event.tags?.map {
                        Chip(requireContext()).apply {
                            text = it
                            isClickable = false
                        }
                    }
                    chips?.forEach {
                        binding.tagChipGroup.addView(it)
                    }

                    Glide.with(binding.thumbImage)
                            .load(event.thumbUrl)
                            .apply(RequestOptions().fitCenter())
                            .into(binding.thumbImage)
                }
            })
            detailsViewModel.getRelatedEvents().observe(viewLifecycleOwner, Observer {
                if (it != null) {
                    Log.d(TAG, "update related events")
                    relatedEventsAdapter.submitList(it)
                }
                if (it?.isNotEmpty() == true) {
                    binding.relatedItemsText.visibility = View.VISIBLE
                } else {
                    binding.relatedItemsText.visibility = View.GONE
                }
            })
            detailsViewModel.getBookmarkForEvent()
                    .observe(viewLifecycleOwner, Observer { watchlistItem: WatchlistItem? ->
                        Log.d(TAG, "Update bookmark")
                        val shouldInvalidate = this@EventDetailsFragment.watchlistItem == null || watchlistItem == null
                        this@EventDetailsFragment.watchlistItem = watchlistItem
                        if (shouldInvalidate) {
                            activity?.invalidateOptionsMenu()
                        }
                    })
        }

        binding.relatedItemsList.apply {
            relatedEventsAdapter = EventRecyclerViewAdapter {
                detailsViewModel.relatedEventSelected(it)
            }
            relatedEventsAdapter.showConferenceName = true
            adapter = relatedEventsAdapter
            val columns: Int = resources.getInteger(R.integer.num_columns)
            layoutManager = if (columns == 1) {
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            } else {
                StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL)
            }
        }

        binding.appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val v = abs(verticalOffset).toDouble() / appBarLayout.totalScrollRange
            if (appBarExpanded xor (v > 0.8)) {
                requireActivity().invalidateOptionsMenu()
                appBarExpanded = v > 0.8
//                binding.collapsingToolbar.isTitleEnabled = appBarExpanded
            }
        })

        detailsViewModel.state.observe(viewLifecycleOwner, Observer { state ->
            if (state == null) {
                return@Observer
            }
            when (state) {
                is DetailsViewModel.State.PlayOfflineItem -> {
                    playItem(state.event, state.recording, state.localFile)
                }
                is DetailsViewModel.State.PlayOnlineItem -> {
                    playItem(state.event,state.recording)
                }
                is DetailsViewModel.State.SelectRecording -> {
                    selectRecording(state.event, state.recordings){ e, r ->
                        detailsViewModel.recordingSelected(e, r)
                    }
                }
                is DetailsViewModel.State.DownloadRecording -> {
                    selectRecording(state.event, state.recordings) { e,r ->
                        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED) {
                                pendingDownload = Pair(e, r)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestPermissions(
                                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                            WRITE_PERMISSION_REQUEST
                                    )
                                }
                            } else {
                                download(e, r)
                            }
                    }
                }
                is DetailsViewModel.State.DisplayEvent -> {
                    findNavController().navigate(EventDetailsFragmentDirections.actionEventDetailsFragmentSelf(eventGuid = state.event.guid))
                }
                is DetailsViewModel.State.PlayExternal -> {
                    if (state.recordings.size > 1){
                        selectRecording(state.event, state.recordings) {event, recording ->
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(recording.recordingUrl)))
                        }
                    }
                }
                is DetailsViewModel.State.Error -> {
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
                DetailsViewModel.State.LoadingRecordings -> {
                    // TODO: show loading indicator
                }
                is DetailsViewModel.State.OpenCustomTab -> {
                    CustomTabsIntent.Builder()
                            .setToolbarColor(ResourcesCompat.getColor(resources, R.color.primary, null))
                            .setStartAnimations(requireContext(), android.R.anim.fade_in, android.R.anim.fade_out)
                            .setExitAnimations(requireContext(), android.R.anim.fade_in, android.R.anim.fade_out)
                            .build()
                            .launchUrl(requireContext(), state.uri)
                }
            }
        })
    }

    private fun play() {
        detailsViewModel.playEvent()
    }

    private fun download(event: Event, recording: Recording) {
        this.detailsViewModel.download(event, recording).observe(viewLifecycleOwner, Observer {
            when (it?.state) {
                OfflineItemManager.State.Downloading -> {
                    layout?.let { view ->
                        Snackbar.make(view, resources.getString(R.string.download_started), Snackbar.LENGTH_LONG).show()
                    }
                }
                OfflineItemManager.State.Done -> {}
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == WRITE_PERMISSION_REQUEST) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                pendingDownload?.let {
                    Log.d(TAG, "starting download after permission request")
                    download(it.first, it.second)
                    pendingDownload = null
                }
            } else {
                layout?.let {
                    Snackbar.make(it, resources.getString(R.string.storage_permission_required), Snackbar.LENGTH_LONG).show()
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun playItem(event: Event, recording: Recording, localFile: String? = null) {
        if (localFile != null) {
            findNavController().navigate(EventDetailsFragmentDirections.actionEventDetailsFragmentToPlayerActivity(PlaybackItem.fromEvent(event,
                    recordingUri = localFile)))
        } else {
            findNavController().navigate(EventDetailsFragmentDirections.actionEventDetailsFragmentToPlayerActivity(PlaybackItem.fromEvent(event,
                    recording.recordingUrl)))
        }
    }

    private fun selectRecording(event: Event, recordings: List<Recording>, action: (Event, Recording) -> Unit) {
        if (detailsViewModel.autoselectRecording) {
            val optimalRecording = ChaosflixUtil.getOptimalRecording(recordings, event.originalLanguage)
            action.invoke(event, optimalRecording)
        } else {
            val items: List<String> = recordings.map { getStringForRecording(it) }
            selectRecordingFromList(items, DialogInterface.OnClickListener { _, i ->
                action.invoke(event, recordings[i])
            })
        }
    }

    private fun selectRecordingFromList(items: List<String>, resultHandler: DialogInterface.OnClickListener) {
        if (selectDialog != null) {
            selectDialog?.dismiss()
        }
        val builder = AlertDialog.Builder(requireContext())
        builder.setItems(items.toTypedArray(), resultHandler)
        selectDialog = builder.create()
        selectDialog?.show()
    }

    private fun getStringForRecording(recording: Recording): String {
        return "${if (recording.isHighQuality) "HD" else "SD"}  ${recording.folder}  [${recording.language}]"
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        Log.d(TAG, "OnPrepareOptionsMenu")
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_bookmark).isVisible = watchlistItem == null
        menu.findItem(R.id.action_unbookmark).isVisible = watchlistItem != null
// 		menu.findItem(R.id.action_download).isVisible = viewModel.writeExternalStorageAllowed
// 		viewModel.offlineItemExists(event).observe(viewLivecycleOwner, Observer { itemExists->
// 					itemExists?.let {exists ->
// 						menu.findItem(R.id.action_download).isVisible =
// 								viewModel.writeExternalStorageAllowed && !exists
// 						menu.findItem(R.id.action_delete_offline_item).isVisible =
// 								viewModel.writeExternalStorageAllowed && exists
// 					}
// 				})

        menu.findItem(R.id.action_play).isVisible = appBarExpanded
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        // 		if (appBarExpanded)
        inflater.inflate(R.menu.details_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                findNavController().apply {
                    popBackStack(R.id.eventsListFragment, false) ||
                            navigateUp()
                }
                return true
            }
            R.id.action_play -> {
                play()
                return true
            }
            R.id.action_bookmark -> {
                detailsViewModel.createBookmark()
                return true
            }
            R.id.action_unbookmark -> {
                detailsViewModel.removeBookmark()
//                activity?.invalidateOptionsMenu()
                return true
            }
            R.id.action_download -> {
                detailsViewModel.downloadRecordingForEvent()
                return true
            }
            R.id.action_delete_offline_item -> {
                detailsViewModel.deleteOfflineItem().observe(viewLifecycleOwner, Observer { success ->
                    if (success != null) {
                        view?.let { Snackbar.make(it, "Deleted Download", Snackbar.LENGTH_SHORT).show() }
                    }
                })
                return true
            }
            R.id.action_share -> {
                lifecycleScope.launch {
                    val event = detailsViewModel.event.value
                    if (event != null) {
                        val shareIntent = Intent(Intent.ACTION_SEND, Uri.parse(event.frontendLink))
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_description))
                        shareIntent.putExtra(Intent.EXTRA_TEXT, event.frontendLink)
                        shareIntent.type = "text/plain"
                        startActivity(shareIntent)
                    } else {
                        view?.let {
                            Snackbar.make(it, resources.getString(R.string.share_info_error), Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
                return true
            }
            R.id.action_external_player -> {
                detailsViewModel.playInExternalPlayer()
                return true
            }
            R.id.action_link -> {
                detailsViewModel.openLink()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    interface OnEventDetailsFragmentInteractionListener {
        fun onToolbarStateChange()
    }

    companion object {
        private val TAG = EventDetailsFragment::class.java.simpleName
        private const val EVENT_PARAM = "event"
        private const val WRITE_PERMISSION_REQUEST = 23

        fun newInstance(event: Event): EventDetailsFragment {
            val fragment = EventDetailsFragment()
            val args = Bundle()
            args.putParcelable(EVENT_PARAM, event)
            fragment.arguments = args
            return fragment
        }
    }
}
