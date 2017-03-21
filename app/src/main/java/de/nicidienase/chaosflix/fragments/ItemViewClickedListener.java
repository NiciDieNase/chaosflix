package de.nicidienase.chaosflix.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.ActivityOptionsCompat;

import de.nicidienase.chaosflix.activities.DetailsActivity;
import de.nicidienase.chaosflix.activities.EventsActivity;
import de.nicidienase.chaosflix.entities.Conference;
import de.nicidienase.chaosflix.entities.Event;

/**
 * Created by felix on 21.03.17.
 */
final class ItemViewClickedListener implements OnItemViewClickedListener {

	private Fragment fragment;

	public ItemViewClickedListener(Fragment fragment) {
		this.fragment = fragment;
	}

	@Override
	public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
							  RowPresenter.ViewHolder rowViewHolder, Row row) {
		if (item instanceof Conference) {
			Conference conference = (Conference) item;
			// Start EventsActivity for this conference
			Intent i = new Intent(fragment.getActivity(), EventsActivity.class);
			i.putExtra(EventsActivity.CONFERENCE, conference);
			i.putExtra(EventsActivity.CONFERENCE_ID, conference.getApiID());
//				Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
//						getActivity(),
//						((ImageCardView) itemViewHolder.view).getMainImageView(),
//						EventsActivity.SHARED_ELEMENT_NAME).toBundle();
			fragment.startActivity(i);

		} else if(item instanceof Event){
			Event event = (Event) item;
			Intent i = new Intent(fragment.getActivity(), DetailsActivity.class);
			i.putExtra(DetailsActivity.EVENT,event);
			Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
					fragment.getActivity(),
					((ImageCardView) itemViewHolder.view).getMainImageView(),
					DetailsActivity.SHARED_ELEMENT_NAME).toBundle();
			fragment.getActivity().startActivity(i, bundle);
		}
	}
}
