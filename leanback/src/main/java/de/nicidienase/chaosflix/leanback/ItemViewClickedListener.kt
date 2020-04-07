package de.nicidienase.chaosflix.leanback

import android.util.Log
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import de.nicidienase.chaosflix.LeakCanaryLauncher
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.Room
import de.nicidienase.chaosflix.leanback.detail.DetailsActivity
import de.nicidienase.chaosflix.leanback.detail.DetailsActivity.Companion.start
import de.nicidienase.chaosflix.leanback.events.EventsActivity
import de.nicidienase.chaosflix.leanback.events.EventsActivity.Companion.start
import de.nicidienase.chaosflix.leanback.settings.ChaosflixSettingsActivity

class ItemViewClickedListener(private val fragment: Fragment) : OnItemViewClickedListener {
    override fun onItemClicked(itemViewHolder: Presenter.ViewHolder, item: Any, rowViewHolder: RowPresenter.ViewHolder?, row: Row?) {
        Log.d(TAG, "onItemClicked")
        val activity = fragment.requireActivity()
        when (item) {
            is Conference -> {
                val transition = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                        (itemViewHolder.view as ImageCardView).mainImageView,
                        EventsActivity.SHARED_ELEMENT_NAME).toBundle()
                // 			EventsActivity.start(fragment.requireContext(), conference, transition);
                start(fragment.requireContext(), item)
            }
            is Event -> {
                val transistion = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                        (itemViewHolder.view as ImageCardView).mainImageView,
                        DetailsActivity.SHARED_ELEMENT_NAME).toBundle()
                start(fragment.requireContext(), item, transistion)
            }
            is Room -> {
                val transition = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                        (itemViewHolder.view as ImageCardView).mainImageView,
                        DetailsActivity.SHARED_ELEMENT_NAME).toBundle()
                start(fragment.requireContext(), item, transition)
            }
            is SelectableContentItem -> {
                when (item) {
                    SelectableContentItem.Settings -> {
                        ChaosflixSettingsActivity.launch(fragment.requireContext())
                    }
                    SelectableContentItem.About -> {
                    }
                    SelectableContentItem.LeakCanary -> {
                        if(BuildConfig.DEBUG){
                            LeakCanaryLauncher.launch(fragment.requireContext())
                        }
                    }
                }
            }
        }
    }

    companion object {
        private val TAG = ItemViewClickedListener::class.java.simpleName
    }
}
