package de.nicidienase.chaosflix;

import android.content.Context;
import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;
import android.support.v17.leanback.widget.Presenter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.ButterKnife;
import de.nicidienase.chaosflix.entities.recording.Event;
import de.nicidienase.chaosflix.entities.streaming.Room;

/**
 * Created by felix on 18.03.17.
 */

public class EventDetailsDescriptionPresenter extends Presenter {

	private final Context mContext;

	public EventDetailsDescriptionPresenter(Context context){
		mContext = context;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent) {
		View view = LayoutInflater.from(mContext).inflate(R.layout.detail_view_content, null);
        return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder vh, Object item) {
		TextView titleText = ButterKnife.findById(vh.view,R.id.title_text);
		TextView speakersText = ButterKnife.findById(vh.view,R.id.speakers_text);
		TextView subtitleText = ButterKnife.findById(vh.view,R.id.subtitle_text);
		TextView descriptionText = ButterKnife.findById(vh.view,R.id.description_text);
		if(item instanceof Event){
			Event event = (Event) item;

			titleText.setText(event.getTitle());
			subtitleText.setText(event.getSubtitle());
			String speaker = TextUtils.join(", ", event.getPersons());
			speakersText.setText(speaker);
			StringBuilder sb = new StringBuilder();
			sb.append(event.getDescription())
					.append("\n")
					.append("\nreleased at: ").append(event.getReleaseDate())
					.append("\nTags: ").append(android.text.TextUtils.join(", ",event.getTags()));
			descriptionText.setText(sb.toString());
		} else if(item instanceof Room){
			Room room = (Room) item;
			titleText.setText(room.getDisplay());
			subtitleText.setText(room.getShedulename());
		}
	}

	@Override
	public void onUnbindViewHolder(ViewHolder viewHolder) {

	}
}
