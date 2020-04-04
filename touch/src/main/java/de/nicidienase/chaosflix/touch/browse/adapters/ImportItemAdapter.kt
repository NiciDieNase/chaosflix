package de.nicidienase.chaosflix.touch.browse.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.nicidienase.chaosflix.common.ImportItem
import de.nicidienase.chaosflix.touch.databinding.ItemFavoritImportBinding
import java.lang.NumberFormatException

class ImportItemAdapter(private val onListItemClick: ((ImportItem) -> Unit)? = null) : ListAdapter<ImportItem, ImportItemAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<ImportItem>() {
        override fun areItemsTheSame(oldItem: ImportItem, newItem: ImportItem) = oldItem === newItem
        override fun areContentsTheSame(oldItem: ImportItem, newItem: ImportItem): Boolean {
            return oldItem.selected == newItem.selected &&
                    oldItem.lecture == newItem.lecture &&
                    oldItem.event == newItem.event
        }
}) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemFavoritImportBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.item = item
        holder.binding.importItemEvent.setOnClickListener {
            holder.binding.checkBox.apply {
                isChecked = !isChecked
            }
            onListItemClick?.invoke(item)
        }
    }

    override fun getItemId(position: Int): Long {
        return try {
            getItem(position).lecture.lectureId?.toLong() ?: 0
        } catch (ex: NumberFormatException) {
            -1
        }
    }

    class ViewHolder(val binding: ItemFavoritImportBinding) : RecyclerView.ViewHolder(binding.root)
}
