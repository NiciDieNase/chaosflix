package de.nicidienase.chaosflix.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by felix on 11.03.17.
 */

public class Event extends SugarRecord implements Parcelable, Comparable<Event> {

	long parentConferenceID;
	String guid;
	String title;
	String subtitle;
	String slug;
	String link;
	String description;
	@SerializedName("original_language")
	String originalLanguage;
	List<String> persons;
	List<String> tags;
	String date;
	@SerializedName("release_date")
	String releaseDate;
	@SerializedName("updated_at")
	String updatedAt;
	long length;
	@SerializedName("thumb_url")
	String thumbUrl;
	@SerializedName("poster_url")
	String posterUrl;
	@SerializedName("frontend_link")
	String frontendLink;
	String url;
	@SerializedName("conference_url")
	String conferenceUrl;
	List<Recording> recordings;


	protected Event(Parcel in) {
		guid = in.readString();
		title = in.readString();
		subtitle = in.readString();
		slug = in.readString();
		link = in.readString();
		description = in.readString();
		originalLanguage = in.readString();
		persons = in.createStringArrayList();
		tags = in.createStringArrayList();
		date = in.readString();
		updatedAt = in.readString();
		length = in.readLong();
		thumbUrl = in.readString();
		posterUrl = in.readString();
		frontendLink = in.readString();
		url = in.readString();
		conferenceUrl = in.readString();
	}

	public static final Creator<Event> CREATOR = new Creator<Event>() {
		@Override
		public Event createFromParcel(Parcel in) {
			return new Event(in);
		}

		@Override
		public Event[] newArray(int size) {
			return new Event[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeString(guid);
		parcel.writeString(title);
		parcel.writeString(subtitle);
		parcel.writeString(slug);
		parcel.writeString(link);
		parcel.writeString(description);
		parcel.writeString(originalLanguage);
		parcel.writeStringList(persons);
		parcel.writeStringList(tags);
		parcel.writeString(date);
		parcel.writeString(updatedAt);
		parcel.writeLong(length);
		parcel.writeString(thumbUrl);
		parcel.writeString(posterUrl);
		parcel.writeString(frontendLink);
		parcel.writeString(url);
		parcel.writeString(conferenceUrl);
	}

	public int getApiID(){
		String[] strings = getUrl().split("/");
		return Integer.parseInt(strings[strings.length-1]);
	}

	public long getParentConferenceID() {
		return parentConferenceID;
	}

	public void setParentConferenceID(long parentConferenceID) {
		this.parentConferenceID = parentConferenceID;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOriginalLanguage() {
		return originalLanguage;
	}

	public void setOriginalLanguage(String originalLanguage) {
		this.originalLanguage = originalLanguage;
	}

	public List<String> getPersons() {
		return persons;
	}

	public void setPersons(List<String> persons) {
		this.persons = persons;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public String getThumbUrl() {
		return thumbUrl;
	}

	public void setThumbUrl(String thumbUrl) {
		this.thumbUrl = thumbUrl;
	}

	public String getPosterUrl() {
		return posterUrl;
	}

	public void setPosterUrl(String posterUrl) {
		this.posterUrl = posterUrl;
	}

	public String getFrontendLink() {
		return frontendLink;
	}

	public void setFrontendLink(String frontendLink) {
		this.frontendLink = frontendLink;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getConferenceUrl() {
		return conferenceUrl;
	}

	public void setConferenceUrl(String conferenceUrl) {
		this.conferenceUrl = conferenceUrl;
	}

	public List<Recording> getRecordings() {
		return recordings;
	}

	public void setRecordings(List<Recording> recordings) {
		this.recordings = recordings;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Override
	public int compareTo(Event event) {
		return this.date.compareTo(event.getDate());
	}

	public void update(Event e) {
		if(!this.updatedAt.equals(e.getUpdatedAt())){
			// TODO actually update
			this.save();
		}
	}
}
