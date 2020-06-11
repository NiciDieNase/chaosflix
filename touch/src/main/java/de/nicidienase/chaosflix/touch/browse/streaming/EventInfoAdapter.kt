package de.nicidienase.chaosflix.touch.browse.streaming

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.nicidienase.chaosflix.common.mediadata.entities.eventinfo.EventInfo
import de.nicidienase.chaosflix.touch.databinding.ItemEventinfoBinding

class EventInfoAdapter(private val clickListener: (EventInfo) -> Unit): ListAdapter<EventInfo, EventInfoAdapter.ViewHolder>(diffUtilCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEventinfoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.item = item
    }

    inner class ViewHolder(val binding: ItemEventinfoBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                clickListener.invoke(getItem(bindingAdapterPosition))
            }
        }
    }

    companion object {
        private val diffUtilCallback = object : DiffUtil.ItemCallback<EventInfo>() {
            override fun areItemsTheSame(oldItem: EventInfo, newItem: EventInfo): Boolean =
                    oldItem=== newItem

            override fun areContentsTheSame(oldItem: EventInfo, newItem: EventInfo): Boolean =
                    oldItem == newItem

        }
    }
}