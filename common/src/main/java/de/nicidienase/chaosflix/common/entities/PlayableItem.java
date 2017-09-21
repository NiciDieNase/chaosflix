package de.nicidienase.chaosflix.common.entities;

import android.os.Parcelable;

import java.util.List;

/**
 * Created by felix on 22.09.17.
 */

public interface PlayableItem extends Parcelable {
	String getTitle();
	String getSubtitle();
	String getImageUrl();
	List<String> getPlaybackOptions();
	String getUrlForOption(int index);
}
