package de.nicidienase.chaosflix.touch.browse.download

import android.arch.lifecycle.Observer
import android.databinding.DataBindingUtil
import android.os.Environment
import android.support.design.widget.Snackbar
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.entities.download.OfflineEvent
import de.nicidienase.chaosflix.databinding.ItemOfflineEventBinding
import de.nicidienase.chaosflix.touch.OnEventSelectedListener
import de.nicidienase.chaosflix.touch.browse.BrowseViewModel
import de.nicidienase.chaosflix.touch.playback.PlayerActivity

class OfflineEventAdapter(var items: List<OfflineEvent>, val viewModel: BrowseViewModel, val listener: OnEventSelectedListener) :
		RecyclerView.Adapter<OfflineEventAdapter.ViewHolder>() {

	override fun onBindViewHolder(holder: OfflineEventAdapter.ViewHolder, position: Int) {
		val item = items[position]
		viewModel.getEventById(item.eventId).observeForever({
			item.event = it
			holder.binding.event = it
			Picasso.with(holder.thumbnail.context)
					.load(item.event?.thumbUrl)
					.noFade()
					.fit()
					.centerInside()
					.into(holder.thumbnail)
		})
		viewModel.getRecordingByid(item.recordingId).observeForever({
			item.recording = it
		}
		)

		holder.binding.downloadStatus = viewModel.downloadStatus[item.downloadReference]
		holder.binding.card.setOnClickListener { view ->
			//			item.event?.let {
//				listener.onEventSelected(it)
//			}
//		}
//
//		holder.binding.textViewOptions.setOnClickListener {
//					holder.binding.card.setOnClickListener {
			val menu = PopupMenu(holder.view.context, holder.binding.card)
			menu.inflate(R.menu.offline_menu)
			menu.setOnMenuItemClickListener { menuItem ->
				when (menuItem.itemId) {
					R.id.item_open -> {
						item.event?.let {
							listener.onEventSelected(it)
						}
						true
					}
					R.id.item_play -> {
						PlayerActivity.launch(view.context, item.event!!, viewModel.getUriForEvent(item))
						true
					}
					R.id.item_delete -> {
						viewModel.deleteOfflineItem(item)
						true
					}
					else -> false
				}

			}
			menu.show()
		}
	}




	override fun getItemCount(): Int {
		return items.size
	}

	override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
		val binding = DataBindingUtil.inflate<ItemOfflineEventBinding>(
				LayoutInflater.from(parent?.context), R.layout.item_offline_event, parent, false)
		return ViewHolder(binding, binding.root)
	}

	inner class ViewHolder(val binding: ItemOfflineEventBinding, val view: View) : RecyclerView.ViewHolder(view) {
		val thumbnail = binding.imageView
	}
}