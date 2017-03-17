package de.nicidienase.chaosflix.entities;

import com.orm.SugarRecord;

import java.util.Date;

/**
 * Created by felix on 17.03.17.
 */

public class Recording extends SugarRecord {
	int size;
	int length;
	String mime_type;
	String language;
	String filename;
	String state;
	String folder;
	boolean high_quality;
	int width;
	int height;
	String updated_at;
	String recording_url;
	String url;
	String event_url;
	String conference_url;

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

	public String getMime_type() {
		return mime_type;
	}

	public void setMime_type(String mime_type) {
		this.mime_type = mime_type;
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

	public boolean isHigh_quality() {
		return high_quality;
	}

	public void setHigh_quality(boolean high_quality) {
		this.high_quality = high_quality;
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

	public String getUpdated_at() {
		return updated_at;
	}

	public void setUpdated_at(String updated_at) {
		this.updated_at = updated_at;
	}

	public String getRecording_url() {
		return recording_url;
	}

	public void setRecording_url(String recording_url) {
		this.recording_url = recording_url;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getEvent_url() {
		return event_url;
	}

	public void setEvent_url(String event_url) {
		this.event_url = event_url;
	}

	public String getConference_url() {
		return conference_url;
	}

	public void setConference_url(String conference_url) {
		this.conference_url = conference_url;
	}
}
