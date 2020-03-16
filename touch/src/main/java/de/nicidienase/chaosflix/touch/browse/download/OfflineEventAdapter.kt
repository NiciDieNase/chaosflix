package de.nicidienase.chaosflix.touch.browse.download

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import de.nicidienase.chaosflix.common.OfflineItemManager
import de.nicidienase.chaosflix.common.userdata.entities.download.OfflineEventView
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.touch.databinding.ItemOfflineEventBinding

class OfflineEventAdapter(
    private val offlineItemManager: OfflineItemManager,
    private val eventDeleteListener: (String) -> Unit,
    private val eventSelectedListener: (String) -> Unit
) :
        RecyclerView.Adapter<OfflineEventAdapter.ViewHolder>() {

    var items: List<OfflineEventView> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.binding.item = item
        Glide.with(holder.thumbnail)
                .load(item.thumbUrl)
                .apply(RequestOptions().fitCenter())
                .into(holder.thumbnail)

        with(holder.binding) {
            downloadStatus = offlineItemManager.downloadStatus[item.downloadReference]
            buttonDelete.setOnClickListener {
                eventDeleteListener(item.eventGuid)
            }
            content?.setOnClickListener { _ ->
                eventSelectedListener(item.eventGuid)
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
        val thumbnail: ImageView = binding.imageView
    }
}
