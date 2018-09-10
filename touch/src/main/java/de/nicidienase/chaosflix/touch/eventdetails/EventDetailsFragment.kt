package de.nicidienase.chaosflix.touch.eventdetails

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.Util
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentRecording
import de.nicidienase.chaosflix.common.userdata.entities.watchlist.WatchlistItem
import de.nicidienase.chaosflix.common.viewmodel.DetailsViewModel
import de.nicidienase.chaosflix.databinding.FragmentEventDetailsBinding
import de.nicidienase.chaosflix.touch.OnEventSelectedListener
import de.nicidienase.chaosflix.common.PreferencesManager
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.touch.browse.adapters.EventRecyclerViewAdapter

class EventDetailsFragment : Fragment() {

	private var listener: OnEventDetailsFragmentInteractionListener? = null

	private var appBarExpanded: Boolean = false
	private lateinit var event: PersistentEvent
	private var watchlistItem: WatchlistItem? = null
	private var eventSelectedListener: OnEventSelectedListener? = null
	private var selectDialog: AlertDialog? = null

	private lateinit var viewModel: DetailsViewModel
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
			val parcelable = arguments?.getParcelable<PersistentEvent>(EVENT_PARAM)
			if(parcelable != null){
				event = parcelable
			} else {
				throw IllegalStateException("Event Missing")
			}
		}
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_event_details, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		val binding = FragmentEventDetailsBinding.bind(view)
		binding.event = event
		binding.playFab.setOnClickListener { _ -> play() }
		if (listener != null) {
			(activity as AppCompatActivity).setSupportActionBar(binding.animToolbar)
			(activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
		}

		eventSelectedListener?.let {
			relatedEventsAdapter = RelatedEventsRecyclerViewAdapter(eventSelectedListener!!)
			binding.relatedItemsList.adapter = relatedEventsAdapter
			binding.relatedItemsList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
		}

		binding.appbar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
			val v = Math.abs(verticalOffset).toDouble() / appBarLayout.totalScrollRange
			if (appBarExpanded xor (v > 0.8)) {
				if (listener != null) {
					listener!!.onToolbarStateChange()
				}
				appBarExpanded = v > 0.8
//                binding.collapsingToolbar.isTitleEnabled = appBarExpanded
			}
		}

		viewModel = ViewModelProviders.of(
				requireActivity(),
				ViewModelFactory(requireContext()))
				.get(DetailsViewModel::class.java)

		viewModel.setEvent(event)
				.observe(this, Observer {
					Log.d(TAG,"Loading Event ${event.title}, ${event.guid}")
					updateBookmark(event.guid)
					binding.thumbImage.transitionName = getString(R.string.thumbnail) + event.guid
					Picasso.with(context)
							.load(event.thumbUrl)
							.noFade()
							.into(binding.thumbImage, object : Callback {
								override fun onSuccess() {
//                                        startPostponedEnterTransition()
								}

								override fun onError() {
//                                        startPostponedEnterTransition()
								}
							})

				})
		viewModel.getRelatedEvents(event).observe(this, Observer {
			relatedEventsAdapter.items = ArrayList(it)
		})

		viewModel.state.observe(this, Observer { liveEvent ->
			if(liveEvent == null){
				return@Observer
			}
			when(liveEvent.state){
				DetailsViewModel.DetailsViewModelState.PlayOfflineItem -> {
					val recording = liveEvent.data?.getString(DetailsViewModel.KEY_LOCAL_PATH)
					if(recording != null){
						listener?.playItem(event, recording)
					}
				}
				DetailsViewModel.DetailsViewModelState.PlayOnlineItem -> {
					liveEvent.data?.getParcelable<PersistentRecording>(DetailsViewModel.KEY_PLAY_RECORDING)?.let {
						listener?.playItem(event,it)
					}

				}
				DetailsViewModel.DetailsViewModelState.SelectRecording -> {
					val selectItems: Array<PersistentRecording> =
							liveEvent.data?.getParcelableArray(DetailsViewModel.KEY_SELECT_RECORDINGS) as Array<PersistentRecording>
					selectRecording(selectItems.asList()) {
						viewModel.playRecording(it)
					}
				}
				DetailsViewModel.DetailsViewModelState.Error -> liveEvent.error?.let { showSnackbar(it) }
			}
		})
	}

	private fun updateBookmark(guid: String) {
		viewModel.getBookmarkForEvent(guid)
				.observe(this, Observer { watchlistItem: WatchlistItem? ->
					this.watchlistItem = watchlistItem
					listener?.invalidateOptionsMenu()
				})
	}

	private fun showSnackbar(message: String, duration: Int = Snackbar.LENGTH_LONG ){
		view?.let {
			Snackbar.make(it, message, duration)
		}
	}

	private fun play() {
		if(listener == null){
			return
		}
		viewModel.playEvent(event)
	}

	private fun selectRecording(persistentRecordings: List<PersistentRecording>, action: (recording: PersistentRecording) -> Unit) {
		val stream = Util.getOptimalStream(persistentRecordings)
		if (stream != null && PreferencesManager.getAutoselectStream()) {
			action.invoke(stream)
		} else {
			val items: List<String> = persistentRecordings.map { getStringForRecording(it) }
			selectRecordingFromList(items, DialogInterface.OnClickListener { dialogInterface, i ->
				action.invoke(persistentRecordings[i])
			})
		}
	}

	private fun getStringForRecording(recording: PersistentRecording): String {
		return "${if (recording.isHighQuality) "HD" else "SD"}  ${recording.folder}  [${recording.language}]"
	}

	private fun selectRecordingFromList(items: List<String>, resultHandler: DialogInterface.OnClickListener) {
		this.context?.let { context ->
			if (selectDialog != null) {
				selectDialog?.dismiss()
			}
			val builder = AlertDialog.Builder(context)
			builder.setItems(items.toTypedArray(), resultHandler)
			selectDialog = builder.create()
			selectDialog?.show()
		}
	}

	override fun onAttach(context: Context?) {
		super.onAttach(context)
		if (context is OnEventSelectedListener) {
			eventSelectedListener = context
		}
		if (context is OnEventDetailsFragmentInteractionListener) {
			listener = context
		} else {
			throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
		}
	}

	override fun onDetach() {
		super.onDetach()
		listener = null
	}

	override fun onPrepareOptionsMenu(menu: Menu) {
		Log.d(TAG, "OnPrepareOptionsMenu")
		super.onPrepareOptionsMenu(menu)
		menu.findItem(R.id.action_bookmark).isVisible = watchlistItem == null
		menu.findItem(R.id.action_unbookmark).isVisible = watchlistItem != null
		menu.findItem(R.id.action_download).isVisible = viewModel.writeExternalStorageAllowed
		viewModel.offlineItemExists(event).observe(this, Observer { itemExists->
					itemExists?.let {exists ->
						menu.findItem(R.id.action_download).isVisible =
								viewModel.writeExternalStorageAllowed && !exists
						menu.findItem(R.id.action_delete_offline_item).isVisible =
								viewModel.writeExternalStorageAllowed && exists
					}
				})

		menu.findItem(R.id.action_play).isVisible = appBarExpanded
	}

	override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
		super.onCreateOptionsMenu(menu, inflater)
		//		if (appBarExpanded)
		inflater!!.inflate(R.menu.details_menu, menu)
	}

	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		when (item!!.itemId) {
			android.R.id.home -> {
				activity?.finish()
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
				listener!!.invalidateOptionsMenu()
				return true
			}
			R.id.action_download -> {
				viewModel.getRecordingForEvent(event).observe(this, Observer { recordings ->
					if (recordings != null) {
						selectRecording(recordings, { recording -> downloadRecording(recording) })
					}
				})
				return true
			}
			R.id.action_delete_offline_item -> {
				viewModel.deleteOfflineItem(event).observe(this, Observer { success ->
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
				shareIntent.setType("text/plain")
				startActivity(shareIntent)
				return true
			}
			R.id.action_external_player -> {
				viewModel.getRecordingForEvent(event).observe(this, Observer { recordings ->
					if (recordings != null) {
						selectRecording(recordings) { recording ->
							val shareIntent = Intent(Intent.ACTION_VIEW, Uri.parse(recording.recordingUrl))
							startActivity(shareIntent)
						}
					}
				})
				return true
			}
			else -> return super.onOptionsItemSelected(item)
		}
	}

	private fun downloadRecording(recording: PersistentRecording) {
		viewModel.download(event, recording).observe(this, Observer {
			if (it != null) {
				val message = if (it) "Download started" else "Error starting download"
				Snackbar.make(view!!, message, Snackbar.LENGTH_LONG).show()
			}
		})
	}

	interface OnEventDetailsFragmentInteractionListener {
		fun onToolbarStateChange()
		fun invalidateOptionsMenu()
		fun playItem(event: PersistentEvent, recording: PersistentRecording)
		fun playItem(event: PersistentEvent, uri: String)
	}

	companion object {
		private val TAG = EventDetailsFragment::class.java.simpleName
		private val EVENT_PARAM = "event_param"

		fun newInstance(event: PersistentEvent): EventDetailsFragment {
			val fragment = EventDetailsFragment()
			val args = Bundle()
			args.putParcelable(EVENT_PARAM, event)
			fragment.arguments = args
			return fragment
		}
	}
}