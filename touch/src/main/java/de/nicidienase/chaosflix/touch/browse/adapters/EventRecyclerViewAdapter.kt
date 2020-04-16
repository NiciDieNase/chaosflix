package de.nicidienase.chaosflix.touch.browse.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.touch.databinding.ItemEventCardviewBinding

open class EventRecyclerViewAdapter(val listener: (Event) -> Unit) : ListAdapter<Event, EventRecyclerViewAdapter.ViewHolder>(EventDiffUtil) {
    object EventDiffUtil : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean = oldItem.guid == newItem.guid
        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean = oldItem == newItem
    }

    var showConferenceName: Boolean = false

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }

    override fun onCreateViewHolder(p0: ViewGroup, pItemConferenceCardviewBinding1: Int): ViewHolder {
        val binding = ItemEventCardviewBinding.inflate(LayoutInflater.from(p0.context), p0, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = getItem(position)
        holder.binding.event = event
        holder.binding.root.setOnClickListener {
            listener(event)
        }
        if (showConferenceName) {
            holder.binding.conferenceNameText.visibility = View.VISIBLE
        }

        ViewCompat.setTransitionName(holder.binding.titleText, "title_${event.guid}")
        ViewCompat.setTransitionName(holder.binding.subtitleText, "subtitle_${event.guid}")
        ViewCompat.setTransitionName(holder.binding.imageView, "thumb_${event.guid}")
    }

    inner class ViewHolder(val binding: ItemEventCardviewBinding) : RecyclerView.ViewHolder(binding.root)
}
