package de.nicidienase.chaosflix;

import android.content.Context;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.Presenter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import de.nicidienase.chaosflix.entities.recording.Conference;
import de.nicidienase.chaosflix.entities.streaming.LiveConference;

/**
 * Created by felix on 16.04.17.
 */

public class HeaderItemPresenter extends Presenter {
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) parent.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.header_item_layout, null);

		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, Object item) {
		HeaderItem headerItem = ((ListRow) item).getHeaderItem();
		View view = viewHolder.view;
		ImageView headerIcon = (ImageView) view.findViewById(R.id.header_icon);
		TextView headerLabel = (TextView) view.findViewById(R.id.header_label);

		if(item instanceof Conference){
			Conference con = (Conference) item;

			headerLabel.setText(con.getTitle());
		} else if(item instanceof LiveConference){
			LiveConference con = (LiveConference) item;

			headerLabel.setText(con.getConference());
		}
	}

	@Override
	public void onUnbindViewHolder(ViewHolder viewHolder) {

	}
}
