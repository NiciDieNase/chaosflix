package de.nicidienase.chaosflix.touch.eventdetails

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.userdata.entities.watchlist.WatchlistItem
import de.nicidienase.chaosflix.common.viewmodel.DetailsViewModel
import de.nicidienase.chaosflix.touch.databinding.FragmentEventDetailsBinding
import de.nicidienase.chaosflix.touch.OnEventSelectedListener
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.touch.browse.adapters.EventRecyclerViewAdapter

class EventDetailsFragment : Fragment() {

    private var listener: OnEventDetailsFragmentInteractionListener? = null

    private var appBarExpanded: Boolean = false
    private lateinit var event: Event
    private var watchlistItem: WatchlistItem? = null
    private var eventSelectedListener: OnEventSelectedListener? = null

    private var layout: View? = null

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
        if (listener != null) {
            (activity as AppCompatActivity).setSupportActionBar(binding.animToolbar)
            (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        binding.relatedItemsList.apply {
            relatedEventsAdapter = EventRecyclerViewAdapter {
                viewModel.relatedEventSelected(it)
            }
            adapter = relatedEventsAdapter
            val orientation = LinearLayoutManager.VERTICAL
            layoutManager = LinearLayoutManager(context, orientation, false)
            val itemDecoration = DividerItemDecoration(binding.relatedItemsList.context, orientation)
            addItemDecoration(itemDecoration)
        }

        binding.appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val v = Math.abs(verticalOffset).toDouble() / appBarLayout.totalScrollRange
            if (appBarExpanded xor (v > 0.8)) {
                listener?.onToolbarStateChange()
                appBarExpanded = v > 0.8
//                binding.collapsingToolbar.isTitleEnabled = appBarExpanded
            }
        })

        viewModel = ViewModelProviders.of(
                requireActivity(),
                ViewModelFactory(requireContext()))
                .get(DetailsViewModel::class.java)

        viewModel.setEvent(event)
                .observe(this, Observer {
                    Log.d(TAG, "Loading Event ${event.title}, ${event.guid}")
                    updateBookmark(event.guid)
                    binding.thumbImage.transitionName = getString(R.string.thumbnail) + event.guid

                    Glide.with(binding.thumbImage)
                            .load(event.thumbUrl)
                            .apply(RequestOptions().fitCenter())
                            .into(binding.thumbImage)
                })
        viewModel.getRelatedEvents(event).observe(this, Observer {
            if (it != null) {
                relatedEventsAdapter.items = it
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

    private fun play() {
        viewModel.playEvent(event)
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
// 		menu.findItem(R.id.action_download).isVisible = viewModel.writeExternalStorageAllowed
// 		viewModel.offlineItemExists(event).observe(this, Observer { itemExists->
// 					itemExists?.let {exists ->
// 						menu.findItem(R.id.action_download).isVisible =
// 								viewModel.writeExternalStorageAllowed && !exists
// 						menu.findItem(R.id.action_delete_offline_item).isVisible =
// 								viewModel.writeExternalStorageAllowed && exists
// 					}
// 				})

        menu.findItem(R.id.action_play).isVisible = appBarExpanded
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        // 		if (appBarExpanded)
        inflater?.inflate(R.menu.details_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
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
                listener?.invalidateOptionsMenu()
                return true
            }
            R.id.action_download -> {
                viewModel.downloadRecordingForEvent(event)
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
        fun invalidateOptionsMenu()
    }

    companion object {
        private val TAG = EventDetailsFragment::class.java.simpleName
        private const val EVENT_PARAM = "event_param"

        fun newInstance(event: Event): EventDetailsFragment {
            val fragment = EventDetailsFragment()
            val args = Bundle()
            args.putParcelable(EVENT_PARAM, event)
            fragment.arguments = args
            return fragment
        }
    }
}
