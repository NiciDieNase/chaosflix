package de.nicidienase.chaosflix.leanback.fragments

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v17.leanback.app.DetailsSupportFragment
import android.support.v17.leanback.widget.Action
import android.support.v17.leanback.widget.ArrayObjectAdapter
import android.support.v17.leanback.widget.ClassPresenterSelector
import android.support.v17.leanback.widget.DetailsOverviewRow
import android.support.v17.leanback.widget.FullWidthDetailsOverviewRowPresenter
import android.support.v17.leanback.widget.FullWidthDetailsOverviewSharedElementHelper
import android.support.v17.leanback.widget.ListRow
import android.support.v17.leanback.widget.ListRowPresenter
import android.support.v17.leanback.widget.OnActionClickedListener
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.Room
import de.nicidienase.chaosflix.common.viewmodel.DetailsViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.leanback.EventDetailsDescriptionPresenter
import de.nicidienase.chaosflix.leanback.activities.ConferencesActivity
import de.nicidienase.chaosflix.leanback.activities.DetailsActivity
import de.nicidienase.chaosflix.leanback.activities.EventDetailsActivity
import de.nicidienase.chaosflix.leanback.activities.PlayerActivity


class NewEventsDetailsFragment : DetailsSupportFragment() {

	private lateinit var viewModel: DetailsViewModel

	private var eventType: Int = DetailsActivity.TYPE_RECORDING
	private var event: PersistentEvent? = null

	private lateinit var rowsAdapter: ArrayObjectAdapter

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		viewModel = ViewModelProviders.of(this, ViewModelFactory(requireContext())).get(DetailsViewModel::class.java)

		eventType = activity!!.intent.getIntExtra(DetailsActivity.TYPE, -1)
		event = requireActivity().intent.getParcelableExtra<PersistentEvent>(DetailsActivity.EVENT)

		if (eventType != DetailsActivity.TYPE_RECORDING || event == null) {
			return
		}

		title = event?.title

		val selector = ClassPresenterSelector()
		val detailsPresenter = FullWidthDetailsOverviewRowPresenter(
				EventDetailsDescriptionPresenter(requireContext()))

		val helper = FullWidthDetailsOverviewSharedElementHelper()
		helper.setSharedElementEnterTransition(activity,
				EventDetailsActivity.SHARED_ELEMENT_NAME)
		detailsPresenter.setListener(helper)
		prepareEntranceTransition()

		detailsPresenter.onActionClickedListener = DetailActionClickedListener()

		selector.addClassPresenter(DetailsOverviewRow::class.java, detailsPresenter)
		selector.addClassPresenter(ListRow::class.java,
				ListRowPresenter())
		rowsAdapter = ArrayObjectAdapter(selector)

		val detailsOverview = DetailsOverviewRow(event)
		Glide.with(activity)
				.load(event?.thumbUrl)
				.asBitmap()
				.into(object : SimpleTarget<Bitmap>(DETAIL_THUMB_WIDTH, DETAIL_THUMB_HEIGHT) {
					override fun onResourceReady(resource: Bitmap,
					                             glideAnimation: GlideAnimation<in Bitmap>) {
						detailsOverview.setImageBitmap(requireActivity(), resource)
					}

					override fun onLoadFailed(e: Exception?, errorDrawable: Drawable?) {
						//						super.onLoadFailed(e, errorDrawable);
						detailsOverview.setImageDrawable(resources.getDrawable(DEFAULT_DRAWABLE))
					}
				})

		val actionAdapter = ArrayObjectAdapter()

		val playAction = Action(ACTION_PLAY, "Play")
		actionAdapter.add(playAction)

		val watchlistAction = Action(ACTION_ADD_WATCHLIST, getString(R.string.add_to_watchlist))
		actionAdapter.add(watchlistAction)
		event?.guid?.let {
			viewModel.getBookmarkForEvent(it).observe(this, Observer { watchlistItem ->
				if (watchlistItem != null) {
					watchlistAction.id = ACTION_REMOVE_WATCHLIST
					watchlistAction.label1 = getString(R.string.remove_from_watchlist)
					actionAdapter.notifyItemRangeChanged(actionAdapter.indexOf(watchlistAction),1)
				} else {
					watchlistAction.id = ACTION_ADD_WATCHLIST
					watchlistAction.label1 = getString(R.string.add_to_watchlist)
					actionAdapter.notifyItemRangeChanged(actionAdapter.indexOf(watchlistAction),1)
				}
			})
		}

		detailsOverview.actionsAdapter = actionAdapter
		rowsAdapter.add(detailsOverview);

		adapter = rowsAdapter

		Handler().postDelayed(this::startEntranceTransition, 500);
	}

	companion object {
		@JvmStatic
		val TAG = NewEventsDetailsFragment::class.java.simpleName

		@JvmStatic
		private val DETAIL_THUMB_WIDTH = 254
		@JvmStatic
		private val DETAIL_THUMB_HEIGHT = 143
		@JvmStatic
		val DEFAULT_DRAWABLE = R.drawable.default_background


		@JvmStatic
		private val ACTION_PLAY: Long = 0
		@JvmStatic
		private val ACTION_ADD_WATCHLIST: Long = 1
		@JvmStatic
		private val ACTION_REMOVE_WATCHLIST: Long = 2
	}

	private inner class DetailActionClickedListener : OnActionClickedListener {
		override fun onActionClicked(action: Action) {
			Log.d(TAG, "OnActionClicked")
			if (action.id == ACTION_ADD_WATCHLIST) {
				event?.guid?.let { viewModel.createBookmark(it) }
				val preferences = requireActivity().getSharedPreferences(getString(R.string.watchlist_preferences_key), Context.MODE_PRIVATE)
				if (preferences.getBoolean(getString(R.string.watchlist_dialog_needed), true)) { // new item
					showWatchlistInfoDialog(preferences)
				}
			} else if (action.id == ACTION_REMOVE_WATCHLIST) {
				event?.guid?.let { viewModel.removeBookmark(it) }
			} else if (action.id == ACTION_PLAY) {
				// TODO play!
				Toast.makeText(requireContext(), "Play", Toast.LENGTH_SHORT).show()
			}
		}

		fun showWatchlistInfoDialog(preferences: SharedPreferences) {
			val builder = AlertDialog.Builder(activity)
			builder.setTitle(R.string.watchlist_message)
			builder.setNegativeButton(R.string.return_to_homescreen) { dialog, which ->
				val i = Intent(activity, ConferencesActivity::class.java)
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
				startActivity(i)
				//						getActivity().finish();
			}
			builder.setPositiveButton("OK") { dialog, which -> }
			val edit = preferences.edit()
			edit.putBoolean(getString(R.string.watchlist_dialog_needed), false)
			edit.apply()

			builder.create().show()
		}
	}
}