package de.nicidienase.chaosflix.touch.browse.download

import android.app.DownloadManager
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
			holder.titleText.text = item.event?.title
			holder.subtitleText.text = item.event?.subtitle
			Picasso.with(holder.thumbnail.context)
					.load(item.event?.thumbUrl)
					.noFade()
					.fit()
					.centerInside()
					.into(holder.thumbnail)
		})
		viewModel.getRecordingByid(item.recordingId)

		val cursor = viewModel.downloadManager.query(DownloadManager.Query().setFilterById(item.downloadReference))

		if (cursor.moveToFirst()) {
			val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
			val status = cursor.getInt(columnIndex)
//		val columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
//		val reason = cursor.getInt(columnReason)
			val bytesSoFarIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
			val bytesSoFar = cursor.getInt(bytesSoFarIndex)
			val bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
			val bytesTotal = cursor.getInt(bytesTotalIndex)

			val statusText: String =
			when(status){
				DownloadManager.STATUS_RUNNING -> "Running"
				DownloadManager.STATUS_FAILED -> "Failed"
				DownloadManager.STATUS_PAUSED -> "Paused"
				DownloadManager.STATUS_SUCCESSFUL -> "Successful"
				DownloadManager.STATUS_PENDING -> "Pending"
				else -> "UNKNOWN"
			}
			holder.tagText.text = statusText

//			if (status == DownloadManager.STATUS_RUNNING) {
				holder.progressBar.max = bytesTotal
				holder.progressBar.progress = bytesSoFar
//			} else {
//				holder.progressBar.visibility = View.GONE
//			}
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
		val titleText = binding.titleText
		val subtitleText = binding.subtitleText
		val tagText = binding.tagText
		val thumbnail = binding.imageView
		val progressBar = binding.downloadProgressBar
	}
}