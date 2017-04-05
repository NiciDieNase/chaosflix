package de.nicidienase.chaosflix.entities.recording;

import com.orm.SugarRecord;

/**
 * Created by felix on 06.04.17.
 */

public class PlaybackProgress extends SugarRecord {
	String eventGuid;
	long progress;
	long recordingId;

	public PlaybackProgress() {
	}

	public PlaybackProgress(String eventGuid, long progress, long recordingId) {
		this.eventGuid = eventGuid;
		this.progress = progress;
		this.recordingId = recordingId;
	}

	public String getEventGuid() {
		return eventGuid;
	}

	public void setEventGuid(String eventGuid) {
		this.eventGuid = eventGuid;
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
