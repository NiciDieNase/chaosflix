package de.nicidienase.chaosflix.touch.browse.download

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.userdata.entities.download.OfflineEvent
import de.nicidienase.chaosflix.touch.databinding.ItemOfflineEventBinding
import de.nicidienase.chaosflix.touch.OnEventSelectedListener
import de.nicidienase.chaosflix.common.viewmodel.BrowseViewModel

class OfflineEventAdapter(var items: List<Pair<OfflineEvent, Event>>, val viewModel: BrowseViewModel, val listener: OnEventSelectedListener) :
		RecyclerView.Adapter<OfflineEventAdapter.ViewHolder>() {

	override fun onBindViewHolder(holder: OfflineEventAdapter.ViewHolder, position: Int) {
		val item = items[position]

		holder.binding.event = item.second
		Picasso.with(holder.thumbnail.context)
				.load(item.second.thumbUrl)
				.noFade()
				.fit()
				.centerInside()
				.into(holder.thumbnail)


		with(holder.binding){
			downloadStatus = viewModel.offlineItemManager.downloadStatus[item.first.downloadReference]
			buttonDelete.setOnClickListener {
				viewModel.deleteOfflineItem(item.first)
			}
			content?.setOnClickListener { _ ->
				listener.onEventSelected(item.second)

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