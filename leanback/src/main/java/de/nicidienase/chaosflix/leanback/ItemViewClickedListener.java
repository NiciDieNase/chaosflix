package de.nicidienase.chaosflix.leanback;

import android.os.Bundle;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.util.Log;

import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference;
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event;
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.Room;
import de.nicidienase.chaosflix.leanback.detail.DetailsActivity;
import de.nicidienase.chaosflix.leanback.events.EventsActivity;

public class ItemViewClickedListener implements OnItemViewClickedListener {

	private static final String   TAG = ItemViewClickedListener.class.getSimpleName();
	private              Fragment fragment;

	public ItemViewClickedListener(Fragment fragment) {
		this.fragment = fragment;
	}

	@Override
	public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
		Log.d(TAG, "onItemClicked");
		FragmentActivity activity = fragment.requireActivity();
		if (item instanceof Conference) {
			Conference conference = (Conference) item;
			Bundle transition = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
			                                                                       ((ImageCardView) itemViewHolder.view).getMainImageView(),
			                                                                       EventsActivity.Companion.getSHARED_ELEMENT_NAME()).toBundle();
//			EventsActivity.start(fragment.requireContext(), conference, transition);
			EventsActivity.start(fragment.requireContext(),conference);
		} else if (item instanceof Event) {
			Bundle transistion = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
			                                                                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
			                                                                        DetailsActivity.Companion.getSHARED_ELEMENT_NAME()).toBundle();
			Event event = (Event) item;
			DetailsActivity.start(fragment.requireContext(), event, transistion);
		} else if (item instanceof Room) {
			Bundle transition = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
			                                                                       ((ImageCardView) itemViewHolder.view).getMainImageView(),
			                                                                       DetailsActivity.Companion.getSHARED_ELEMENT_NAME()).toBundle();
			Room room = (Room) item;
			DetailsActivity.start(fragment.requireContext(), room, transition);
		}
	}
}
