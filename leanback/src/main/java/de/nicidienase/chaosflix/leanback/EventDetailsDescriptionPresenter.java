package de.nicidienase.chaosflix.leanback

import android.content.Context
import android.support.v17.leanback.widget.Presenter
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import butterknife.ButterKnife
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.entities.recording.Event
import de.nicidienase.chaosflix.common.entities.streaming.Room
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.Room

/**
 * Created by felix on 18.03.17.
 */

class EventDetailsDescriptionPresenter(private val mContext: Context) : Presenter() {

	override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
		val view = LayoutInflater.from(mContext).inflate(R.layout.detail_view_content, null)
		return Presenter.ViewHolder(view)
	}

	override fun onBindViewHolder(vh: Presenter.ViewHolder, item: Any) {
		val titleText = ButterKnife.findById(vh.view, R.id.title_text)
		val speakersText = ButterKnife.findById(vh.view, R.id.speakers_text)
		val subtitleText = ButterKnife.findById(vh.view, R.id.subtitle_text)
		val descriptionText = ButterKnife.findById(vh.view, R.id.description_text)
		if (item is PersistentEvent) {
			val (_, _, _, _, title, subtitle, _, _, description, _, _, releaseDate, _, _, _, _, _, _, _, _, _, persons, tags) = item

			titleText.setText(title)
			subtitleText.setText(subtitle)
			val speaker = TextUtils.join(", ", persons!!)
			speakersText.setText(speaker)
			val sb = StringBuilder()
			sb.append(description)
					.append("\n")
					.append("\nreleased at: ").append(releaseDate)
					.append("\nTags: ").append(android.text.TextUtils.join(", ", tags!!))
			descriptionText.setText(sb.toString())
		} else if (item is Room) {
			val (_, schedulename, _, _, display) = item
			titleText.setText(display)
			subtitleText.setText(schedulename)
		}
	}

	override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {

	}

}
