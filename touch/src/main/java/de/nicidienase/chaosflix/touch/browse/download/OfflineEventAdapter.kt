package de.nicidienase.chaosflix.touch.browse.download

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import de.nicidienase.chaosflix.common.OfflineItemManager
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.userdata.entities.download.OfflineEvent
import de.nicidienase.chaosflix.touch.databinding.ItemOfflineEventBinding
import de.nicidienase.chaosflix.touch.OnEventSelectedListener
import de.nicidienase.chaosflix.common.viewmodel.BrowseViewModel

class OfflineEventAdapter(private val offlineItemManager: OfflineItemManager,
                          private val eventDeleteListener: (OfflineEvent) -> Unit,
                          private val eventSelectedListener: (Event)->Unit) :
		RecyclerView.Adapter<OfflineEventAdapter.ViewHolder>() {

	var items: List<Pair<OfflineEvent, Event>> = emptyList()
		set(value) {
			field = value
			notifyDataSetChanged()
		}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val item = items[position]

		holder.binding.event = item.second
		Glide.with(holder.thumbnail)
				.load(item.second.thumbUrl)
				.apply(RequestOptions().fitCenter())
				.into(holder.thumbnail)

		with(holder.binding){
			downloadStatus = offlineItemManager.downloadStatus[item.first.downloadReference]
			buttonDelete.setOnClickListener {
				eventDeleteListener(item.first)
			}
			content?.setOnClickListener { _ ->
				eventSelectedListener(item.second)

			}
		}
	}

	override fun getItemCount(): Int {
		return items.size
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val binding = DataBindingUtil.inflate<ItemOfflineEventBinding>(
				LayoutInflater.from(parent.context), R.layout.item_offline_event, parent, false)
		return ViewHolder(binding, binding.root)
	}

	inner class ViewHolder(val binding: ItemOfflineEventBinding, val view: View) : RecyclerView.ViewHolder(view) {
		val thumbnail = binding.imageView
	}
}