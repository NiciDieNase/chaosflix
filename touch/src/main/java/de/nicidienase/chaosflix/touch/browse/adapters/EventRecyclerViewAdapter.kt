package de.nicidienase.chaosflix.touch.browse.adapters

import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.touch.databinding.ItemEventCardviewBinding

open class EventRecyclerViewAdapter(val listener: (Event) -> Unit) :
        ItemRecyclerViewAdapter<Event, EventRecyclerViewAdapter.ViewHolder>() {

    override fun getComparator(): Comparator<in Event>? {
        return Comparator { o1, o2 -> o1.title.compareTo(o2.title) }
    }

    override fun getItemId(position: Int): Long {
        return items[position].id
    }

    override fun getFilteredProperties(item: Event): List<String> {
        return listOfNotNull(item.title,
                item.subtitle,
                item.description,
                item.getSpeakerString()
        )
    }

    override fun onCreateViewHolder(p0: ViewGroup, pItemConferenceCardviewBinding1: Int): ViewHolder {
        val binding = ItemEventCardviewBinding.inflate(LayoutInflater.from(p0.context), p0, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = items[position]
        holder.binding.event = event
        holder.binding.root.setOnClickListener {
            listener(event)
        }

        ViewCompat.setTransitionName(holder.binding.titleText, "title_${event.guid}")
        ViewCompat.setTransitionName(holder.binding.subtitleText, "subtitle_${event.guid}")
        ViewCompat.setTransitionName(holder.binding.imageView, "thumb_${event.guid}")
    }

    inner class ViewHolder(val binding: ItemEventCardviewBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)
}
