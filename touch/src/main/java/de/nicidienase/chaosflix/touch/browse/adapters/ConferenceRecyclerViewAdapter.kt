package de.nicidienase.chaosflix.touch.browse.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.touch.browse.ConferencesTabBrowseFragment
import de.nicidienase.chaosflix.touch.databinding.ItemConferenceCardviewBinding

class ConferenceRecyclerViewAdapter(private val mListener: ConferencesTabBrowseFragment.OnInteractionListener?) :
        androidx.recyclerview.widget.RecyclerView.Adapter<ConferenceRecyclerViewAdapter.ViewHolder>() {

    var conferences: List<Conference> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = conferences.size

    class ViewHolder(val binding: ItemConferenceCardviewBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)

    override fun getItemId(position: Int): Long {
        return conferences.get(position).id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemConferenceCardviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.conference = conferences[position]

        holder.binding.root.setOnClickListener { _ ->
            mListener?.onConferenceSelected((conferences[position]))
        }
    }
}
