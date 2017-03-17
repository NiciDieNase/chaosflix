package de.nicidienase.chaosflix.entities;

import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by felix on 17.03.17.
 */

public class Conferences extends SugarRecord{
	List<Conference> conferences;

	public List<Conference> getConferences() {
		return conferences;
	}

	public void setConferences(List<Conference> conferences) {
		this.conferences = conferences;
	}
}
