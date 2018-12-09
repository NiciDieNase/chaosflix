package de.nicidienase.chaosflix.leanback;

import android.os.Bundle;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentConference;
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentEvent;
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.Room;
import de.nicidienase.chaosflix.leanback.activities.DetailsActivity;
import de.nicidienase.chaosflix.leanback.activities.EventsActivity;

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
		if (item instanceof PersistentConference) {
			PersistentConference conference = (PersistentConference) item;
			Bundle transition = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
			                                                                       ((ImageCardView) itemViewHolder.view).getMainImageView(),
			                                                                       EventsActivity.Companion.getSHARED_ELEMENT_NAME()).toBundle();
			EventsActivity.start(fragment.requireContext(), conference, transition);
			//			EventsActivity.start(fragment.requireContext(),conference);
		} else if (item instanceof PersistentEvent) {
			Bundle transistion = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
			                                                                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
			                                                                        DetailsActivity.Companion.getSHARED_ELEMENT_NAME()).toBundle();
			PersistentEvent event = (PersistentEvent) item;
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
