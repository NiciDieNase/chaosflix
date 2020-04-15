package de.nicidienase.chaosflix.touch.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.touch.databinding.ItemEventCardviewBinding

class SearchResultAdapter(val listener: (Event) -> Unit) :
    PagedListAdapter<Event, SearchResultAdapter.ViewHolder>(EventDiffCallback) {

    companion object {
        val EventDiffCallback = object : DiffUtil.ItemCallback<Event>() {
            override fun areItemsTheSame(oldItem: Event, newItem: Event) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Event, newItem: Event) = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, pItemConferenceCardviewBinding1: Int): ViewHolder {
        val binding = ItemEventCardviewBinding.inflate(LayoutInflater.from(p0.context), p0, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = getItem(position)
        holder.binding.event = event
        holder.binding.root.setOnClickListener {
            event?.let {
                listener(event)
            }
        }
        event?.let {
            ViewCompat.setTransitionName(holder.binding.titleText, "title_${it.guid}")
            ViewCompat.setTransitionName(holder.binding.subtitleText, "subtitle_${it.guid}")
            ViewCompat.setTransitionName(holder.binding.imageView, "thumb_${it.guid}")
        }
    }

    inner class ViewHolder(val binding: ItemEventCardviewBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)
}