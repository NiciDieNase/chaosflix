package de.nicidienase.chaosflix.touch.eventdetails

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.Util
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentRecording
import de.nicidienase.chaosflix.common.entities.userdata.WatchlistItem
import de.nicidienase.chaosflix.databinding.FragmentEventDetailsBinding
import de.nicidienase.chaosflix.touch.OnEventSelectedListener
import de.nicidienase.chaosflix.touch.ViewModelFactory
import de.nicidienase.chaosflix.touch.browse.adapters.EventRecyclerViewAdapter

class EventDetailsFragment : Fragment() {

	private var listener: OnEventDetailsFragmentInteractionListener? = null

	private var eventId: Long = 0
	private var appBarExpanded: Boolean = false
	private lateinit var event: PersistentEvent
	private var watchlistItem: WatchlistItem? = null
	private var eventSelectedListener: OnEventSelectedListener? = null

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
			eventId = arguments!!.getLong(EVENT_PARAM)
		}
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_event_details, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		val binding = FragmentEventDetailsBinding.bind(view)
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

		viewModel = ViewModelProviders.of(activity!!, ViewModelFactory).get(DetailsViewModel::class.java)


		viewModel.getEventById(eventId)
				.observe(this, Observer { event: PersistentEvent? ->
					if (event != null) {
						this.event = event
						updateBookmark()
						binding.event = event
						binding.thumbImage.transitionName = getString(R.string.thumbnail) + event.eventId
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


						val relatedIds: LongArray = event.metadata?.related?.keys?.toLongArray() ?: longArrayOf()

						viewModel.getEventsByIds(relatedIds)
								.observe(this, Observer { events ->
									relatedEventsAdapter.items = ArrayList(events)
								})

					}
				})
	}

	private fun updateBookmark() {
		viewModel.getBookmarkForEvent(eventId)
				.observe(this, Observer { watchlistItem: WatchlistItem? ->
					this.watchlistItem = watchlistItem
					listener?.invalidateOptionsMenu()
				})
	}

	private fun play() {
		listener?.let {
			viewModel.getOfflineItem(eventId).observe(this, Observer { offlineEvent ->
				if(offlineEvent != null){
					Log.d(TAG,"Playing offline file")
					listener?.playItem(event,offlineEvent.localPath)
				} else {
					viewModel.getRecordingForEvent(eventId)
							.observe(this, Observer { persistentRecordings ->
								if (persistentRecordings != null) {
									Log.d(TAG,"Playing network file")
									listener!!.playItem(event, Util.getOptimalStream(persistentRecordings))
								}
							})
				}
			})
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
		if (watchlistItem != null) {
			menu.findItem(R.id.action_bookmark).isVisible = false
			menu.findItem(R.id.action_unbookmark).isVisible = true
		} else {
			menu.findItem(R.id.action_bookmark).isVisible = true
			menu.findItem(R.id.action_unbookmark).isVisible = false
		}
		menu.findItem(R.id.action_download).isVisible = viewModel.writeExternalStorageAllowed
		viewModel.offlineItemExists(eventId).observe(this,
				Observer { itemExists ->
					itemExists?.let {
						menu.findItem(R.id.action_download).isVisible =
								viewModel.writeExternalStorageAllowed && !itemExists
						menu.findItem(R.id.action_delete_offline_item).isVisible =
								viewModel.writeExternalStorageAllowed && itemExists
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
				viewModel.createBookmark(eventId)
				updateBookmark()
				return true
			}
			R.id.action_unbookmark -> {
				viewModel.removeBookmark(eventId)
				watchlistItem = null
				listener!!.invalidateOptionsMenu()
				return true
			}
			R.id.action_download -> {
				viewModel.getRecordingForEvent(eventId).observeForever {
					viewModel.download(event, Util.getOptimalStream(it!!)).observe(this, Observer {
						if (it != null) {
							val message = if (it) "Download started" else "Error starting download"
							Snackbar.make(view!!, message, Snackbar.LENGTH_LONG).show()
						}
					})
				}
				activity?.invalidateOptionsMenu()
				return true
			}
			R.id.action_delete_offline_item -> {
				viewModel.getOfflineItem(eventId).observeForever {
					if (it != null) {
						viewModel.deleteOfflineItem(it)
					}
				}
				return true
			}
			else -> return super.onOptionsItemSelected(item)
		}
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

		fun newInstance(eventId: Long): EventDetailsFragment {
			val fragment = EventDetailsFragment()
			val args = Bundle()
			args.putLong(EVENT_PARAM, eventId)
			fragment.arguments = args
			return fragment
		}
	}
}
