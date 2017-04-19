package de.nicidienase.chaosflix.entities;

import com.orm.SugarRecord;

/**
 * Created by felix on 06.04.17.
 */

public class PlaybackProgress extends SugarRecord {
	int eventId;
	long progress;
	long recordingId;

	public PlaybackProgress() {
	}

	public PlaybackProgress(int eventId, long progress, long recordingId) {
		this.eventId = eventId;
		this.progress = progress;
		this.recordingId = recordingId;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public long getProgress() {
		return progress;
	}

	public void setProgress(long progress) {
		this.progress = progress;
	}

	public long getRecordingId() {
		return recordingId;
	}

	public void setRecordingId(long recordingId) {
		this.recordingId = recordingId;
	}
}
