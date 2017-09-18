package de.nicidienase.chaosflix.common.entities;

import com.orm.SugarRecord;

import org.joda.time.DateTime;

import java.util.Date;

/**
 * Created by felix on 19.04.17.
 */

public class WatchlistItem extends SugarRecord {
	int eventId;
	long timestamp;

	public WatchlistItem() {
	}

	public WatchlistItem(int eventId) {
		this.setId((long) eventId);
		this.eventId = eventId;
		setAdded(new DateTime(new Date()));
	}

	public WatchlistItem(int eventId, DateTime added) {
		this.setId((long) eventId);
		this.eventId = eventId;
		setAdded(added);
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public DateTime getAdded() {
		return new DateTime(timestamp);
	}

	public void setAdded(DateTime added) {
		timestamp = added.getMillis();
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}
