package de.nicidienase.chaosflix.entities;

import com.orm.SugarRecord;

import org.joda.time.DateTime;

import java.util.Date;

/**
 * Created by felix on 19.04.17.
 */

public class WatchlistItem extends SugarRecord {
	int eventId;
	DateTime added;

	public WatchlistItem() {
	}

	public WatchlistItem(int eventId) {
		this.setId((long) eventId);
		this.eventId = eventId;
		this.added = new DateTime(new Date());
	}

	public WatchlistItem(int eventId, DateTime added) {
		this.setId((long) eventId);
		this.eventId = eventId;
		this.added = added;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public DateTime getAdded() {
		return added;
	}

	public void setAdded(DateTime added) {
		this.added = added;
	}
}
