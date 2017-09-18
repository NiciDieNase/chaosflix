package de.nicidienase.chaosflix.touch.fragments;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.shared.entities.recording.Conference;
import de.nicidienase.chaosflix.touch.fragments.ConferenceFragment.OnListFragmentInteractionListener;

import java.util.List;

public class ConferenceRecyclerViewAdapter extends RecyclerView.Adapter<ConferenceRecyclerViewAdapter.ViewHolder> {

	private final List<Conference> mConferences;
	private final OnListFragmentInteractionListener mListener;

	public ConferenceRecyclerViewAdapter(List<Conference> items, OnListFragmentInteractionListener listener) {
		mConferences = items;
		mListener = listener;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.fragment_conference, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position) {
		holder.mItem = mConferences.get(position);
		holder.mTitleText.setText(mConferences.get(position).getTitle());
		holder.mAcronym.setText(mConferences.get(position).getAcronym());
		Glide.with(holder.mIcon.getContext())
				.load(mConferences.get(position).getLogoUrl())
				.fitCenter()
				.into(holder.mIcon);

		holder.mView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (null != mListener) {
					// Notify the active callbacks interface (the activity, if the
					// fragment is attached to one) that an item has been selected.
					mListener.onListFragmentInteraction(holder.mItem);
				}
			}
		});
	}

	@Override
	public int getItemCount() {
		return mConferences.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		public final View mView;
		public final ImageView mIcon;
		public final TextView mTitleText;
		public final TextView mAcronym;
		public Conference mItem;

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
