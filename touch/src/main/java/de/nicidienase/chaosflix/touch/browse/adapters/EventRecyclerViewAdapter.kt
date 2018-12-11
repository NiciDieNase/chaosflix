package de.nicidienase.chaosflix.touch.browse.adapters

import android.support.v4.view.ViewCompat
import android.view.View
import com.squareup.picasso.Picasso
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.touch.OnEventSelectedListener
import java.util.*

open class EventRecyclerViewAdapter(val listener: OnEventSelectedListener) :
		ItemRecyclerViewAdapter<PersistentEvent>() {

	override fun getComparator(): Comparator<in PersistentEvent>? {
		return Comparator { o1, o2 -> o1.title.compareTo(o2.title) }
	}

	override fun getItemId(position: Int): Long {
		return items.get(position).id
	}

	override fun getFilteredProperties(item: PersistentEvent): List<String> {
		return listOf(item.title,
				item.subtitle,
				item.description,
				item.getSpeakerString()
		).filterNotNull()
	}


	override val layout = R.layout.item_event_cardview
	var showTags: Boolean = false

	override fun onBindViewHolder(holder: ItemRecyclerViewAdapter<PersistentEvent>.ViewHolder, position: Int) {
		val event = items[position]

		holder.titleText.text = event.title
		holder.subtitle.text = event.subtitle
		if (showTags) {
			val tagString = StringBuilder()
			for (tag in event.tags!!) {
				if (tagString.length > 0) {
					tagString.append(", ")
				}
				tagString.append(tag)
			}
			holder.tag.text = tagString
		}
		Picasso.with(holder.icon.context)
				.load(event.thumbUrl)
				.noFade()
				.fit()
				.centerInside()
				.into(holder.icon)

		val resources = holder.titleText.context.getResources()
		ViewCompat.setTransitionName(holder.titleText,
				resources.getString(R.string.title) + event.id)
		ViewCompat.setTransitionName(holder.subtitle,
				resources.getString(R.string.subtitle) + event.id)
		ViewCompat.setTransitionName(holder.icon,
				resources.getString(R.string.thumbnail) + event.id)

		holder.mView.setOnClickListener({ _: View -> listener.onEventSelected(items[position]) })
	}
}
