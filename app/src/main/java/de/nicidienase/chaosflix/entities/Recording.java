package de.nicidienase.chaosflix.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

/**
 * Created by felix on 17.03.17.
 */

public class Recording extends SugarRecord implements Parcelable {
	private int size;
	private int length;
	@SerializedName("mime_type")
	private String mimeType;
	private String language;
	private String filename;
	private String state;
	private String folder;
	@SerializedName("high_quality")
	boolean highQuality;
	private int width;
	private int height;
	@SerializedName("updated_at")
	private String updatedAt;
	@SerializedName("recording_url")
	private String recordingUrl;
	private String url;
	@SerializedName("event_url")
	private String eventUrl;
	@SerializedName("conference_url")
	private String conferenceUrl;

	protected Recording(Parcel in) {
		size = in.readInt();
		length = in.readInt();
		mimeType = in.readString();
		language = in.readString();
		filename = in.readString();
		state = in.readString();
		folder = in.readString();
		highQuality = in.readByte() != 0;
		width = in.readInt();
		height = in.readInt();
		updatedAt = in.readString();
		recordingUrl = in.readString();
		url = in.readString();
		eventUrl = in.readString();
		conferenceUrl = in.readString();
	}

	public static final Creator<Recording> CREATOR = new Creator<Recording>() {
		@Override
		public Recording createFromParcel(Parcel in) {
			return new Recording(in);
		}

		@Override
		public Recording[] newArray(int size) {
			return new Recording[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeInt(size);
		parcel.writeInt(length);
		parcel.writeString(mimeType);
		parcel.writeString(language);
		parcel.writeString(filename);
		parcel.writeString(state);
		parcel.writeString(folder);
		parcel.writeByte((byte) (highQuality ? 1 : 0));
		parcel.writeInt(width);
		parcel.writeInt(height);
		parcel.writeString(updatedAt);
		parcel.writeString(recordingUrl);
		parcel.writeString(url);
		parcel.writeString(eventUrl);
		parcel.writeString(conferenceUrl);
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public boolean isHighQuality() {
		return highQuality;
	}

	public void setHighQuality(boolean highQuality) {
		this.highQuality = highQuality;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getRecordingUrl() {
		return recordingUrl;
	}

	public void setRecordingUrl(String recordingUrl) {
		this.recordingUrl = recordingUrl;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getEventUrl() {
		return eventUrl;
	}

	public void setEventUrl(String eventUrl) {
		this.eventUrl = eventUrl;
	}

	public String getConferenceUrl() {
		return conferenceUrl;
	}

	public void setConferenceUrl(String conferenceUrl) {
		this.conferenceUrl = conferenceUrl;
	}
}
