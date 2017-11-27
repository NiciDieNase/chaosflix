package de.nicidienase.chaosflix.touch.browse.download

import android.app.DownloadManager
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.entities.download.OfflineEvent
import de.nicidienase.chaosflix.databinding.ItemOfflineEventBinding
import de.nicidienase.chaosflix.touch.browse.BrowseViewModel

class OfflineEventAdapter(var items: List<OfflineEvent>, val viewModel: BrowseViewModel) :
		RecyclerView.Adapter<OfflineEventAdapter.ViewHolder>() {

	override fun onBindViewHolder(holder: OfflineEventAdapter.ViewHolder, position: Int) {
		val item = items[position]
		viewModel.getEventById(item.eventId).observeForever(Observer {
			item.event = it
			holder.binding.event = it
			Picasso.with(holder.thumbnail.context)
					.load(item.event?.thumbUrl)
					.noFade()
					.fit()
					.centerInside()
					.into(holder.thumbnail)
		})
//		viewModel.getRecordingByid(item.recordingId)

		holder.binding.downloadStatus = viewModel.downloadStatus[item.downloadReference]

//			if (status == DownloadManager.STATUS_RUNNING) {
//		holder.progressBar.max = downloadStatus?.totalBytes ?: 0
//		holder.progressBar.progress = downloadStatus?.currentBytes ?: 0
//			} else {
//				holder.progressBar.visibility = View.GONE
//			}
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