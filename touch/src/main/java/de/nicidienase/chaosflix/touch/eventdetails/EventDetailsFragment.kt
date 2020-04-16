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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.touch.browse.adapters.EventRecyclerViewAdapter
import de.nicidienase.chaosflix.touch.databinding.FragmentEventDetailsBinding
import de.nicidienase.chaosflix.touch.playback.PlaybackItem
import kotlinx.coroutines.launch

class EventDetailsFragment : Fragment() {

    private var appBarExpanded: Boolean = false
    private var watchlistItem: WatchlistItem? = null

    private var layout: View? = null

    private lateinit var viewModel: DetailsViewModel
    private var selectDialog: AlertDialog? = null
    private var pendingDownload: Bundle? = null

    private lateinit var relatedEventsAdapter: EventRecyclerViewAdapter

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

        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance(requireContext()))
            .get(DetailsViewModel::class.java)

        val eventGuid = args.eventGuid
        val eventName = args.eventName
        lifecycleScope.launch {
            val eventLivedata = when {
                eventGuid != null -> viewModel.setEventByGuid(eventGuid)
                eventName != null -> viewModel.setEventFromLink(eventName)
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
            viewModel.getRelatedEvents().observe(viewLifecycleOwner, Observer {
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
            viewModel.getBookmarkForEvent()
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
                viewModel.relatedEventSelected(it)
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
            val v = Math.abs(verticalOffset).toDouble() / appBarLayout.totalScrollRange
            if (appBarExpanded xor (v > 0.8)) {
                requireActivity().invalidateOptionsMenu()
                appBarExpanded = v > 0.8
//                binding.collapsingToolbar.isTitleEnabled = appBarExpanded
            }
        })

        viewModel.state.observe(viewLifecycleOwner, Observer { liveEvent ->
            if (liveEvent == null) {
                return@Observer
            }
            val event = liveEvent.data?.getParcelable<Event>(DetailsViewModel.EVENT)
            val recording = liveEvent.data?.getParcelable<Recording>(DetailsViewModel.RECORDING)
            val localFile = liveEvent.data?.getString(DetailsViewModel.KEY_LOCAL_PATH)
            val selectItems: List<Recording>? = liveEvent.data?.getParcelableArrayList<Recording>(DetailsViewModel.KEY_SELECT_RECORDINGS)
            when (liveEvent.state) {
                DetailsViewModel.State.PlayOfflineItem -> {
                    if (event != null && recording != null) {
                        playItem(event, recording, localFile)
                    }
                }
                DetailsViewModel.State.PlayOnlineItem -> {
                    if (event != null && recording != null) {
                        playItem(event, recording)
                    }
                }
                DetailsViewModel.State.SelectRecording -> {
                    if (event != null && selectItems != null) {
                        selectRecording(event, selectItems) { e, r ->
                            viewModel.recordingSelected(e, r)
                        }
                    }
                }
                DetailsViewModel.State.DownloadRecording -> {
                    if (event != null && selectItems != null) {
                        selectRecording(event, selectItems) { e, r ->
                            viewModel.download(e, r).observe(viewLifecycleOwner, Observer {
                                when (it?.state) {
                                    OfflineItemManager.State.Downloading -> {
                                        Snackbar.make(binding.root, "Download started", Snackbar.LENGTH_LONG).show()
                                    }
                                    OfflineItemManager.State.PermissionRequired -> {
                                        pendingDownload = liveEvent.data
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                                    WRITE_PERMISSION_REQUEST)
                                        }
                                    }
                                    OfflineItemManager.State.Done -> {}
                                }
                            })
                        }
                    }
                }
                DetailsViewModel.State.DisplayEvent -> {
                    event?.let {
                        findNavController().navigate(EventDetailsFragmentDirections.actionEventDetailsFragmentSelf(event.guid))
                    }
                }
                DetailsViewModel.State.PlayExternal -> {
                    recording?.recordingUrl?.let {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
                    }
                }
                DetailsViewModel.State.Error -> {
                    Snackbar.make(binding.root, liveEvent.error ?: "An Error occured", Snackbar.LENGTH_LONG)
                }
                DetailsViewModel.State.LoadingRecordings -> {
                    // TODO: show loading indicator
                }
            }
        })
    }

    private fun updateBookmark() {
    }

    private fun play() {
        viewModel.playEvent()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == WRITE_PERMISSION_REQUEST) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                pendingDownload?.let {
                    val recording = it.getParcelable<Recording>(DetailsViewModel.RECORDING)
                    val event = it.getParcelable<Event>(DetailsViewModel.EVENT)
                    if (event != null && recording != null) {
                        viewModel.download(event, recording)
                    }
                }
                pendingDownload = null
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun playItem(event: Event, recording: Recording, localFile: String? = null) {
//        if (castService.connected) {
//            castService.loadMediaAndPlay(recording, event)
//        } else {
        if (localFile != null) {
            findNavController().navigate(EventDetailsFragmentDirections.actionEventDetailsFragmentToPlayerActivity(PlaybackItem.fromEvent(event,
                    recordingUri = localFile)))
//                PlayerActivity.launch(requireContext(), event, localFile)
        } else {
//                PlayerActivity.launch(requireContext(), event, recording)

            findNavController().navigate(EventDetailsFragmentDirections.actionEventDetailsFragmentToPlayerActivity(PlaybackItem.fromEvent(event,
                    recording.recordingUrl)))
        }
//        }
    }

    private fun selectRecording(event: Event, recordings: List<Recording>, action: (Event, Recording) -> Unit) {
        if (viewModel.autoselectRecording) {
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
                viewModel.createBookmark()
                return true
            }
            R.id.action_unbookmark -> {
                viewModel.removeBookmark()
//                activity?.invalidateOptionsMenu()
                return true
            }
            R.id.action_download -> {
                viewModel.downloadRecordingForEvent()
                return true
            }
            R.id.action_delete_offline_item -> {
                viewModel.deleteOfflineItem().observe(viewLifecycleOwner, Observer { success ->
                    if (success != null) {
                        view?.let { Snackbar.make(it, "Deleted Download", Snackbar.LENGTH_SHORT).show() }
                    }
                })
                return true
            }
            R.id.action_share -> {
                lifecycleScope.launch {
                    val event = viewModel.event.value
                    if (event != null) {
                        val shareIntent = Intent(Intent.ACTION_SEND, Uri.parse(event.frontendLink))
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_description))
                        shareIntent.putExtra(Intent.EXTRA_TEXT, event.frontendLink)
                        shareIntent.type = "text/plain"
                        startActivity(shareIntent)
                    } else {
                        view?.let {
                            Snackbar.make(it, "Could not find share information", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
                return true
            }
            R.id.action_external_player -> {
                viewModel.playInExternalPlayer()
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
