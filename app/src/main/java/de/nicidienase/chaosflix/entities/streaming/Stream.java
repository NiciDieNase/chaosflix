package de.nicidienase.chaosflix.entities.streaming;

import java.util.Map;

/**
 * Created by felix on 23.03.17.
 */

class Stream {
	String slug;
	String display;
	String type;
	boolean isTranslated;
	int[] videoSize;
	Map<String,StreamUrl> urls;

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isTranslated() {
		return isTranslated;
	}

	public void setTranslated(boolean translated) {
		isTranslated = translated;
	}

	public int[] getVideoSize() {
		return videoSize;
	}

	public void setVideoSize(int[] videoSize) {
		this.videoSize = videoSize;
	}

	public Map<String, StreamUrl> getUrls() {
		return urls;
	}

	public void setUrls(Map<String, StreamUrl> urls) {
		this.urls = urls;
	}
}
