package de.nicidienase.chaosflix.entities;

import java.util.List;

/**
 * Created by felix on 23.03.17.
 */

class Room {
	String slug;
	String shedulename;
	String thumb;
	String link;
	String display;
	List<Stream> streams;

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getShedulename() {
		return shedulename;
	}

	public void setShedulename(String shedulename) {
		this.shedulename = shedulename;
	}

	public String getThumb() {
		return thumb;
	}

	public void setThumb(String thumb) {
		this.thumb = thumb;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	public List<Stream> getStreams() {
		return streams;
	}

	public void setStreams(List<Stream> streams) {
		this.streams = streams;
	}
}
