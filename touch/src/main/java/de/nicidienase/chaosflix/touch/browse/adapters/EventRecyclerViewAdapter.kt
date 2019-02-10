package de.nicidienase.chaosflix.touch.browse.adapters

import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.touch.OnEventSelectedListener
import de.nicidienase.chaosflix.touch.databinding.ItemEventCardviewBinding
import java.util.*

open class EventRecyclerViewAdapter(val listener: OnEventSelectedListener) :
		ItemRecyclerViewAdapter<Event, EventRecyclerViewAdapter.ViewHolder>() {
	override fun getComparator(): Comparator<in Event>? {
		return Comparator { o1, o2 -> o1.title.compareTo(o2.title) }
	}

	override fun getItemId(position: Int): Long {
		return items.get(position).id
	}

	override fun getFilteredProperties(item: Event): List<String> {
		return listOf(item.title,
				item.subtitle,
				item.description,
				item.getSpeakerString()
		).filterNotNull()
	}

	override fun onCreateViewHolder(p0: ViewGroup, pItemConferenceCardviewBinding1: Int): ViewHolder {
		val binding = ItemEventCardviewBinding.inflate(LayoutInflater.from(p0.context), p0, false)
		return ViewHolder(binding)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val event = items[position]
		holder.binding.event = event
		holder.binding.root.setOnClickListener {
			listener.onEventSelected(event)
		}

		ViewCompat.setTransitionName(holder.binding.titleText, "title_${event.guid}")
		ViewCompat.setTransitionName(holder.binding.subtitleText, "subtitle_${event.guid}")
		ViewCompat.setTransitionName(holder.binding.imageView, "thumb_${event.guid}")
	}

	inner class ViewHolder(val binding: ItemEventCardviewBinding) : RecyclerView.ViewHolder(binding.root)
}
