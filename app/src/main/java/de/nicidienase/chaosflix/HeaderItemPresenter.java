package de.nicidienase.chaosflix;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.RowHeaderPresenter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import de.nicidienase.chaosflix.fragments.ConferencesBrowseFragment;

/**
 * Created by felix on 16.04.17.
 */

public class HeaderItemPresenter extends RowHeaderPresenter {
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) parent.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.header_item_layout, null);

		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
		HeaderItem headerItem = ((ListRow) item).getHeaderItem();
		View view = viewHolder.view;
		ImageView headerIcon = (ImageView) view.findViewById(R.id.header_icon);
		TextView headerLabel = (TextView) view.findViewById(R.id.header_label);

		if (headerItem.getName().startsWith(ConferencesBrowseFragment.STREAM_PREFIX)) {
			Drawable camIcon = view.getContext().getResources().getDrawable(R.drawable.ic_videocam_white_24dp);
			headerIcon.setImageDrawable(camIcon);
			((ListRow) item).setHeaderItem(new HeaderItem(headerItem.getName()
					.substring(ConferencesBrowseFragment.STREAM_PREFIX.length())));
		} else {
			Drawable movieIcon = view.getContext().getResources().getDrawable(R.drawable.ic_local_movies_white_24dp);
			headerIcon.setImageDrawable(movieIcon);
			headerLabel.setText(headerItem.getName());
		}
	}

	@Override
	public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {

	}
}
