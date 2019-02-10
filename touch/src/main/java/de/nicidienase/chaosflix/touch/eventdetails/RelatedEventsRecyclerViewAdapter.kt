package de.nicidienase.chaosflix.touch.eventdetails

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.touch.OnEventSelectedListener
import de.nicidienase.chaosflix.touch.databinding.RelatedEventCardviewLayoutBinding

class RelatedEventsRecyclerViewAdapter(listener: OnEventSelectedListener) : RecyclerView.Adapter<RelatedEventsRecyclerViewAdapter.ViewHolder>() {

	init {
		setHasStableIds(true)
	}

	var events: List<Event> = emptyList()
		set(value) {
			field = value
			notifyDataSetChanged()
		}

	override fun getItemCount() = events.size

	override fun onCreateViewHolder(p0: ViewGroup, pItemConferenceCardviewBinding1: Int): ViewHolder {
		val binding = RelatedEventCardviewLayoutBinding.inflate(LayoutInflater.from(p0.context), p0, false)
		return ViewHolder(binding)
	}

	override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
		viewHolder.binding.event = events[position]
	}

	inner class ViewHolder(val binding: RelatedEventCardviewLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}
