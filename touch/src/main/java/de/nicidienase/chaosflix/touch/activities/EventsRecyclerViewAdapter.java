package de.nicidienase.chaosflix.touch.activities;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.Event;

/**
 * Created by felix on 19.09.17.
 */

class EventsRecyclerViewAdapter extends RecyclerView.Adapter<EventsRecyclerViewAdapter.ViewHolder> {

	private final List<Event> mEvents;
	private final OnListFragmentInteractionListener mListener;

	public EventsRecyclerViewAdapter(List<Event> items, OnListFragmentInteractionListener listener) {
		mEvents = items;
		mListener = listener;
	}

	@Override
	public EventsRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.cardview_item, parent, false);
		return new EventsRecyclerViewAdapter.ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(final EventsRecyclerViewAdapter.ViewHolder holder, int position) {
		holder.mItem = mEvents.get(position);
		holder.mTitleText.setText(mEvents.get(position).getTitle());
		holder.mAcronym.setText(mEvents.get(position).getSubtitle());
		Glide.with(holder.mIcon.getContext())
				.load(mEvents.get(position).getThumbUrl())
				.fitCenter()
				.into(holder.mIcon);

		holder.mView.setOnClickListener(v -> {
			if (null != mListener) {
				// Notify the active callbacks interface (the activity, if the
				// fragment is attached to one) that an item has been selected.
				mListener.onListItemSelected(holder.mItem);
			}
		});
	}

	@Override
	public int getItemCount() {
		return mEvents.size();
	}

	public interface OnListFragmentInteractionListener {
		public void onListItemSelected(Event event);
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		public final View mView;
		public final ImageView mIcon;
		public final TextView mTitleText;
		public final TextView mAcronym;
		public Event mItem;

		public ViewHolder(View view) {
			super(view);
			mView = view;
			mIcon = (ImageView) view.findViewById(R.id.imageView);
			mTitleText = (TextView) view.findViewById(R.id.title_text);
			mAcronym = (TextView) view.findViewById(R.id.acronym_text);
		}

		@Override
		public String toString() {
			return super.toString() + " '" + mTitleText.getText() + "'";
		}
	}
}
