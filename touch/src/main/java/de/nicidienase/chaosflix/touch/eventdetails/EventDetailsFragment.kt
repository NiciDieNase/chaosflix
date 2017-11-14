package de.nicidienase.chaosflix.touch.eventdetails

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.transition.TransitionInflater
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
import de.nicidienase.chaosflix.touch.browse.BrowseFragment
import de.nicidienase.chaosflix.touch.browse.EventsListFragment
import de.nicidienase.chaosflix.touch.browse.adapters.EventRecyclerViewAdapter

class EventDetailsFragment : BrowseFragment() {

    private var listener: OnEventDetailsFragmentInteractionListener? = null

    private var eventId: Long = 0
    private var appBarExpanded: Boolean = false
    private var event: PersistentEvent? = null
    private var watchlistItem: WatchlistItem? = null
    private var eventSelectedListener: OnEventSelectedListener? = null

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

    private lateinit var relatedEventsAdapter: EventRecyclerViewAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentEventDetailsBinding.bind(view)
        binding.playFab.setOnClickListener { _ -> play() }
        if (listener != null)
            listener!!.setActionbar(binding.animToolbar)

        eventSelectedListener?.let{
            relatedEventsAdapter = RelatedEventsRecyclerViewAdapter(eventSelectedListener!!)
            binding.relatedItemsList.adapter = relatedEventsAdapter
            binding.relatedItemsList.layoutManager = GridLayoutManager(context,2)
        }

        binding.appbar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val v = Math.abs(verticalOffset).toDouble() / appBarLayout.totalScrollRange
            if (appBarExpanded xor (v > 0.8)) {
                if (listener != null) {
                    listener!!.onToolbarStateChange()
                }
                appBarExpanded = v > 0.8
                binding.collapsingToolbar.isTitleEnabled = appBarExpanded
            }
        }

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
            val event = this.event
            if (event != null) {

                viewModel.getRecordingForEvent(eventId)
                        .observe(this, Observer { persistentRecordings ->
                            if (persistentRecordings != null) {
                                listener!!.playItem(event, Util.getOptimalStream(persistentRecordings))
                            }
                        })
            }
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

    override fun onPrepareOptionsMenu(menu: Menu?) {
        Log.d(TAG, "OnPrepareOptionsMenu")
        super.onPrepareOptionsMenu(menu)
        if (watchlistItem != null) {
            menu!!.findItem(R.id.action_bookmark).isVisible = false
            menu.findItem(R.id.action_unbookmark).isVisible = true
        } else {
            menu!!.findItem(R.id.action_bookmark).isVisible = true
            menu.findItem(R.id.action_unbookmark).isVisible = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        //		if (appBarExpanded)
        inflater!!.inflate(R.menu.details_menu, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
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
                Snackbar.make(view!!, "Download not yet implemented", Snackbar.LENGTH_LONG).show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    interface OnEventDetailsFragmentInteractionListener {
        fun onToolbarStateChange()
        fun setActionbar(toolbar: Toolbar)
        fun invalidateOptionsMenu()
        fun playItem(event: PersistentEvent, recording: PersistentRecording)
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
