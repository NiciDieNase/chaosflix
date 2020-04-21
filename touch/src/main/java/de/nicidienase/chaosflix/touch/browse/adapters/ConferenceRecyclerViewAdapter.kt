package de.nicidienase.chaosflix.touch.browse.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.touch.databinding.ItemConferenceCardviewBinding

class ConferenceRecyclerViewAdapter(private val mListener: (Conference) -> Unit) : ListAdapter<Conference, ConferenceRecyclerViewAdapter.ViewHolder>(conferenceDiffUtil) {

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemConferenceCardviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.conference = getItem(position)
        holder.binding.root.setOnClickListener { _ ->
            mListener(getItem(position))
        }
    }

    class ViewHolder(val binding: ItemConferenceCardviewBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private val conferenceDiffUtil = object : ItemCallback<Conference>() {
            override fun areItemsTheSame(oldItem: Conference, newItem: Conference): Boolean = oldItem === newItem
            override fun areContentsTheSame(oldItem: Conference, newItem: Conference): Boolean = oldItem.url == newItem.url
        }
    }
}
