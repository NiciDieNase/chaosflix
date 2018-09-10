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
			Intent i = new Intent(fragment.getActivity(), EventsActivity.class);
			i.putExtra(EventsActivity.CONFERENCE, conference);
			i.putExtra(EventsActivity.CONFERENCE_ACRONYM, conference.getAcronym());
			Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
					activity,
					((ImageCardView) itemViewHolder.view).getMainImageView(),
					EventsActivity.SHARED_ELEMENT_NAME).toBundle();
			fragment.startActivity(i, bundle);
		} else if (item instanceof PersistentEvent) {
			PersistentEvent event = (PersistentEvent) item;
			Intent i = new Intent(fragment.getActivity(), DetailsActivity.class);
			i.putExtra(DetailsActivity.TYPE, DetailsActivity.TYPE_RECORDING);
			i.putExtra(DetailsActivity.EVENT, event);
			Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
					activity,
					((ImageCardView) itemViewHolder.view).getMainImageView(),
					EventDetailsActivity.SHARED_ELEMENT_NAME).toBundle();
			activity.startActivity(i, bundle);
		} else if (item instanceof Room) {
			Room room = (Room) item;
			Intent i = new Intent(fragment.getActivity(), DetailsActivity.class);
			i.putExtra(DetailsActivity.TYPE, DetailsActivity.TYPE_STREAM);
			i.putExtra(DetailsActivity.ROOM, room);
			Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
					activity,
					((ImageCardView) itemViewHolder.view).getMainImageView(),
					EventDetailsActivity.SHARED_ELEMENT_NAME).toBundle();
			activity.startActivity(i, bundle);
		}
	}
}
