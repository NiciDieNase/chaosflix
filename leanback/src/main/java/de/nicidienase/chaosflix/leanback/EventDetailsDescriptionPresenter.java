package de.nicidienase.chaosflix.leanback;

import android.content.Context;
import android.support.v17.leanback.widget.Presenter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentEvent;
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.Room;

/**
 * Created by felix on 18.03.17.
 */

public class EventDetailsDescriptionPresenter extends Presenter {

	private final Context mContext;

	public EventDetailsDescriptionPresenter(Context context) {
		mContext = context;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent) {
//		DetailViewContentBinding.inflate(LayoutInflater.from(mContext));
		View view = LayoutInflater.from(mContext).inflate(R.layout.detail_view_content, null);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder vh, Object item) {
		if (item instanceof PersistentEvent) {
			PersistentEvent event = (PersistentEvent) item;

//			titleText.setText(event.getTitle());
//			subtitleText.setText(event.getSubtitle());
//			String speaker = TextUtils.join(", ", event.getPersons());
//			speakersText.setText(speaker);
//			StringBuilder sb = new StringBuilder();
//			sb.append(event.getDescription())
//					.append("\n")
//					.append("\nreleased at: ").append(event.getReleaseDate())
//					.append("\nTags: ").append(android.text.TextUtils.join(", ", event.getTags()));
//			descriptionText.setText(sb.toString());
//		} else if (item instanceof Room) {
//			Room room = (Room) item;
//			titleText.setText(room.getDisplay());
//			subtitleText.setText(room.getSchedulename());
		}
	}

	public class DetailViewContent {
		private String title;
		private String subtitle;
		private String speakers;
		private String description;

		DetailViewContent(String title, String subtitle, String speakers, String description){

			this.title = title;
			this.subtitle = subtitle;
			this.speakers = speakers;
			this.description = description;
		}

		DetailViewContent(PersistentEvent event){
			this(event.getTitle(),event.getSubtitle(), TextUtils.join(", ", event.getPersons()), "");
			StringBuilder sb = new StringBuilder();
			sb.append(event.getDescription())
					.append("\n")
					.append("\nreleased at: ").append(event.getReleaseDate())
					.append("\nTags: ").append(android.text.TextUtils.join(", ", event.getTags()));
			description = sb.toString();
		}

		DetailViewContent(Room room){
			this(room.getDisplay(),room.getSchedulename(),"","");
		}

		public String getTitle() {
			return title;
		}

		public String getSubtitle() {
			return subtitle;
		}

		public String getSpeakers() {
			return speakers;
		}

		public String getDescription() {
			return description;
		}
	}

	public class DescriptionViewHolder extends ViewHolder{
		public DescriptionViewHolder(View view) {
			super(view);
		}

//		private final DetailviewcontentBinding binding;

//		public DescriptionViewHolder(View view, DetailviewcontentBinding binding) {
//			super(view);
//			this.binding = binding;
//		}

//		public DetailviewcontentBinding getBinding() {
//			return binding;
//		}
	}


	@Override
	public void onUnbindViewHolder(ViewHolder viewHolder) {

	}
}
