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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.AppBarLayout
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
import kotlinx.android.synthetic.main.activity_eventdetails.fragment_container

class EventDetailsFragment : Fragment() {

    private var appBarExpanded: Boolean = false
    private lateinit var event: Event
    private var watchlistItem: WatchlistItem? = null

    private var layout: View? = null

    private lateinit var viewModel: DetailsViewModel
    private var selectDialog: AlertDialog? = null
    private var pendingDownload: Bundle? = null

    private lateinit var relatedEventsAdapter: EventRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
//        postponeEnterTransition()
//        val transition = TransitionInflater.from(context)
//                .inflateTransition(android.R.transition.move)
//        //		transition.setDuration(getResources().getInteger(R.integer.anim_duration));
//        sharedElementEnterTransition = transition

        if (arguments != null) {
            val parcelable = arguments?.getParcelable<Event>(EVENT_PARAM)
            if (parcelable != null) {
                event = parcelable
            } else {
                throw IllegalStateException("Event Missing")
            }
        }
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
        binding.event = event
        binding.playFab.setOnClickListener { play() }
        (activity as AppCompatActivity).setSupportActionBar(binding.animToolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.relatedItemsList.apply {
            relatedEventsAdapter = EventRecyclerViewAdapter {
                viewModel.relatedEventSelected(it)
            }
            adapter = relatedEventsAdapter
            val orientation = RecyclerView.VERTICAL
            layoutManager =
                LinearLayoutManager(context, orientation, false)
            val itemDecoration = DividerItemDecoration(
                binding.relatedItemsList.context,
                orientation
            )
            addItemDecoration(itemDecoration)
        }

        binding.appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val v = Math.abs(verticalOffset).toDouble() / appBarLayout.totalScrollRange
            if (appBarExpanded xor (v > 0.8)) {
                requireActivity().invalidateOptionsMenu()
                appBarExpanded = v > 0.8
//                binding.collapsingToolbar.isTitleEnabled = appBarExpanded
            }
        })

        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance(requireContext()))
            .get(DetailsViewModel::class.java)

        viewModel.setEvent(event)
                .observe(viewLifecycleOwner, Observer {
                    Log.d(TAG, "Loading Event ${event.title}, ${event.guid}")
                    updateBookmark(event.guid)
                    binding.thumbImage.transitionName = getString(R.string.thumbnail) + event.guid

                    Glide.with(binding.thumbImage)
                            .load(event.thumbUrl)
                            .apply(RequestOptions().fitCenter())
                            .into(binding.thumbImage)
                })
        viewModel.getRelatedEvents(event).observe(viewLifecycleOwner, Observer {
            if (it != null) {
                relatedEventsAdapter.items = it
            }
        })

        viewModel.state.observe(viewLifecycleOwner, Observer { liveEvent ->
            if (liveEvent == null) {
                return@Observer
            }
            val event = liveEvent.data?.getParcelable<Event>(DetailsViewModel.EVENT)
            val recording = liveEvent.data?.getParcelable<Recording>(DetailsViewModel.RECORDING)
            val localFile = liveEvent.data?.getString(DetailsViewModel.KEY_LOCAL_PATH)
            val selectItems: Array<Recording>? = liveEvent.data?.getParcelableArray(DetailsViewModel.KEY_SELECT_RECORDINGS) as Array<Recording>?
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
                        selectRecording(event, selectItems.asList()) { e, r ->
                            viewModel.playRecording(e, r)
                        }
                    }
                }
                DetailsViewModel.State.DownloadRecording -> {
                    if (event != null && selectItems != null) {
                        selectRecording(event, selectItems.asList()) { e, r ->
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
                        findNavController().navigate(EventDetailsFragmentDirections.actionEventDetailsFragmentSelf(event))
                    }
                }
                DetailsViewModel.State.PlayExternal -> {
                    if (event != null) {
                        if (selectItems != null) {
                            selectRecording(event, selectItems.asList()) { _, r ->
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(r.recordingUrl)))
                            }
                        } else if (recording != null) {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(recording.recordingUrl)))
                        }
                    }
                }
                DetailsViewModel.State.Error -> {
                    Snackbar.make(binding.root, liveEvent.error ?: "An Error occured", Snackbar.LENGTH_LONG)
                }
            }
        })
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
                findNavController().navigate(EventDetailsFragmentDirections.actionEventDetailsFragmentToExoPlayerFragment(PlaybackItem.fromEvent(event,
                        recordingUri = localFile)))
//                PlayerActivity.launch(requireContext(), event, localFile)
            } else {
//                PlayerActivity.launch(requireContext(), event, recording)

                findNavController().navigate(EventDetailsFragmentDirections.actionEventDetailsFragmentToExoPlayerFragment(PlaybackItem.fromEvent(event,
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

    private fun updateBookmark(guid: String) {
        viewModel.getBookmarkForEvent(guid)
                .observe(viewLifecycleOwner, Observer { watchlistItem: WatchlistItem? ->
                    this.watchlistItem = watchlistItem
                    activity?.invalidateOptionsMenu()
                })
    }

    private fun play() {
        viewModel.playEvent(event)
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
                viewModel.createBookmark(event.guid)
                updateBookmark(event.guid)
                return true
            }
            R.id.action_unbookmark -> {
                viewModel.removeBookmark(event.guid)
                watchlistItem = null
                activity?.invalidateOptionsMenu()
                return true
            }
            R.id.action_download -> {
                viewModel.downloadRecordingForEvent(event)
                return true
            }
            R.id.action_delete_offline_item -> {
                viewModel.deleteOfflineItem(event).observe(viewLifecycleOwner, Observer { success ->
                    if (success != null) {
                        view?.let { Snackbar.make(it, "Deleted Download", Snackbar.LENGTH_SHORT).show() }
                    }
                })
                return true
            }
            R.id.action_share -> {
                val shareIntent = Intent(Intent.ACTION_SEND, Uri.parse(event.frontendLink))
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.watch_this))
                shareIntent.putExtra(Intent.EXTRA_TEXT, event.frontendLink)
                shareIntent.type = "text/plain"
                startActivity(shareIntent)
                return true
            }
            R.id.action_external_player -> {
                viewModel.playInExternalPlayer(event)
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
