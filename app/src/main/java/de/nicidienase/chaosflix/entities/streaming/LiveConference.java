package de.nicidienase.chaosflix.entities.streaming;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by felix on 23.03.17.
 */

public class LiveConference {
	String conference;
	String slug;
	String author;
	String description;
	String keywords;
	String startsAt;
	String endsAt;
	List<Group> groups;

	public LiveConference() {}

	public LiveConference(String conference, String description) {
		this.conference = conference;
		this.description = description;
	}

	public String getConference() {
		return conference;
	}

	public void setConference(String conference) {
		this.conference = conference;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getStartsAt() {
		return startsAt;
	}

	public void setStartsAt(String startsAt) {
		this.startsAt = startsAt;
	}

	public String getEndsAt() {
		return endsAt;
	}

	public void setEndsAt(String endsAt) {
		this.endsAt = endsAt;
	}

	public List<Group> getGroups() {
		return groups;
	}

	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}

	public static LiveConference getDummyObject(){
		LiveConference dummyCon = new LiveConference("DummyCon", "Conference McConferenceface");
		dummyCon.setGroups(new ArrayList<>());
		dummyCon.getGroups().add(Group.getDummyObject());
		dummyCon.setSlug("duco");
		dummyCon.setAuthor("");
		dummyCon.setDescription("A placeholder conference");
		dummyCon.setKeywords("");
		return dummyCon;
	}
}
