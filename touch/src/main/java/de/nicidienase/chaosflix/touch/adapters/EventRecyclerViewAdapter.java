package de.nicidienase.chaosflix.touch.adapters;

import android.content.res.Resources;
import android.support.v4.view.ViewCompat;

import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentConference;
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentEvent;
import de.nicidienase.chaosflix.touch.fragments.EventsListFragment;

public class EventRecyclerViewAdapter extends ItemRecyclerViewAdapter<PersistentEvent> {

	private boolean areTagsUsefull;
	private final EventsListFragment.OnEventsListFragmentInteractionListener mListener;

	public EventRecyclerViewAdapter(EventsListFragment.OnEventsListFragmentInteractionListener listener) {
		super();
		mListener = listener;
	}

	public EventRecyclerViewAdapter(PersistentConference conference, List<PersistentEvent> events, EventsListFragment.OnEventsListFragmentInteractionListener listener) {
		super(events);
		mListener = listener;

		setItems(events);
		areTagsUsefull = conference.getTagsUsefull();
		Collections.sort(mItems, (o1, o2) -> o1.getTitle().compareTo(o2.getTitle()));
	}

	@Override
	public void onBindViewHolder(ItemRecyclerViewAdapter.ViewHolder holder, int position) {
		PersistentEvent event = mItems.get(position);

		holder.mItem = event;
		holder.mTitleText.setText(event.getTitle());
		holder.mSubtitle.setText(event.getSubtitle());
		if (areTagsUsefull) {
			StringBuilder tagString = new StringBuilder();
			for (String tag : event.getTags()) {
				if (tagString.length() > 0) {
					tagString.append(", ");
				}
				tagString.append(tag);
			}
			holder.mTag.setText(tagString);
		}
		Picasso.with(holder.mIcon.getContext())
				.load(event.getThumbUrl())
				.noFade()
				.fit()
				.centerInside()
				.into(holder.mIcon);

		Resources resources = holder.mTitleText.getContext().getResources();
		ViewCompat.setTransitionName(holder.mTitleText,
				resources.getString(R.string.title) + event.getEventId());
		ViewCompat.setTransitionName(holder.mSubtitle,
				resources.getString(R.string.subtitle) + event.getEventId());
		ViewCompat.setTransitionName(holder.mIcon,
				resources.getString(R.string.thumbnail) + event.getEventId());

		holder.mView.setOnClickListener(v -> {
			if (null != mListener) {
				mListener.onEventSelected((PersistentEvent) holder.mItem, v);
			}
		});
	}

	@Override
	int getLayout() {
		return R.layout.event_cardview_layout;
	}
}
