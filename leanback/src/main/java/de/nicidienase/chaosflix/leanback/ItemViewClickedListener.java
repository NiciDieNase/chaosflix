package de.nicidienase.chaosflix.leanback;

import android.content.Intent;
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
import de.nicidienase.chaosflix.leanback.activities.EventDetailsActivity;
import de.nicidienase.chaosflix.leanback.activities.EventsActivity;

public class ItemViewClickedListener implements OnItemViewClickedListener {

	private static final String TAG = ItemViewClickedListener.class.getSimpleName();
	private Fragment fragment;

	public ItemViewClickedListener(Fragment fragment) {
		this.fragment = fragment;
	}

	@Override
	public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
							  RowPresenter.ViewHolder rowViewHolder, Row row) {
		Log.d(TAG, "onItemClicked");
		FragmentActivity activity = fragment.requireActivity();
		if (item instanceof PersistentConference) {
			PersistentConference conference = (PersistentConference) item;
			// Start EventsActivity for this conference
			Bundle transition = ActivityOptionsCompat.makeSceneTransitionAnimation(
					activity,
					((ImageCardView) itemViewHolder.view).getMainImageView(),
					EventsActivity.Companion.getSHARED_ELEMENT_NAME()).toBundle();
			EventsActivity.start(fragment.requireContext(),conference,transition);
		} else if (item instanceof PersistentEvent) {
			PersistentEvent event = (PersistentEvent) item;
			Intent i = new Intent(fragment.getActivity(), DetailsActivity.class);
			i.putExtra(DetailsActivity.Companion.getTYPE(), DetailsActivity.Companion.getTYPE_RECORDING());
			i.putExtra(DetailsActivity.Companion.getEVENT(), event);
			Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
					activity,
					((ImageCardView) itemViewHolder.view).getMainImageView(),
					EventDetailsActivity.Companion.getSHARED_ELEMENT_NAME()).toBundle();
			activity.startActivity(i, bundle);
		} else if (item instanceof Room) {
			Room room = (Room) item;
			Intent i = new Intent(fragment.getActivity(), DetailsActivity.class);
			i.putExtra(DetailsActivity.Companion.getTYPE(), DetailsActivity.Companion.getTYPE_STREAM());
			i.putExtra(DetailsActivity.Companion.getROOM(), room);
			Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
					activity,
					((ImageCardView) itemViewHolder.view).getMainImageView(),
					EventDetailsActivity.Companion.getSHARED_ELEMENT_NAME()).toBundle();
			activity.startActivity(i, bundle);
		}
	}
}
