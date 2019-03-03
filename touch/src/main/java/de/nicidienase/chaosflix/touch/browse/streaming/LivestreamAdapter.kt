package de.nicidienase.chaosflix.touch.browse.streaming


import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.LiveConference
import de.nicidienase.chaosflix.touch.databinding.ItemLiveeventCardviewBinding

class LivestreamAdapter(val listener: LivestreamListFragment.InteractionListener, liveConferences: List<LiveConference> = emptyList()) : RecyclerView.Adapter<LivestreamAdapter.ViewHolder>() {

	lateinit var items: MutableList<StreamingItem>

	init {
		setContent(liveConferences)
	}

	private fun convertToStreamingItemList(liveConferences: List<LiveConference>) {
		liveConferences.map { liveConference ->
			liveConference.groups.map { group ->
				group.rooms.map { room ->
					items.add(StreamingItem(liveConference, group, room))
				}
			}
		}
	}

	private val TAG = LivestreamAdapter::class.simpleName

	fun setContent(liveConferences: List<LiveConference>) {
		items = ArrayList()
		convertToStreamingItemList(liveConferences)
		Log.d(TAG, "Size:" + items.size)
		notifyDataSetChanged()
	}

	override fun getItemCount(): Int {
		return items.size
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val binding =
				ItemLiveeventCardviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return ViewHolder(binding)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val item = items[position]

		holder.binding.item = item
		Glide.with(holder.binding.root)
				.load(item.room.thumb)
				.apply(RequestOptions().fitCenter())
				.into(holder.binding.imageView)

		holder.binding.root.setOnClickListener { listener.onStreamSelected(item) }
	}


	inner class ViewHolder(val binding: ItemLiveeventCardviewBinding) : RecyclerView.ViewHolder(binding.root)

}
