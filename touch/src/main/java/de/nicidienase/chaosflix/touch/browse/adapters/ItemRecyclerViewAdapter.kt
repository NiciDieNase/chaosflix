package de.nicidienase.chaosflix.touch.browse.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import de.nicidienase.chaosflix.touch.R
import java.util.*

abstract class ItemRecyclerViewAdapter<T,VH : RecyclerView.ViewHolder?>()
	: RecyclerView.Adapter<VH>(), Filterable {

	abstract fun getComparator(): Comparator<in T>?

	internal abstract fun getFilteredProperties(item: T): List<String>

	private val _filter by lazy { ItemFilter() }

	override fun getFilter(): Filter {
		return _filter
	}

	private var _items: MutableList<T> = ArrayList<T>()

	private var filteredItems: MutableList<T> = _items

	var items: MutableList<T>
		get() = filteredItems
		set(value) {
			_items = value
			if (getComparator() != null) {
				Collections.sort(_items, getComparator())
			}
			filteredItems = _items
			notifyDataSetChanged()
		}

	fun addItem(item: T) {
		if (items.contains(item)) {
			val index = items.indexOf(item)
			items[index] = item
			notifyItemChanged(index)
		} else {
			items.add(item)
			notifyItemInserted(items.size - 1)
		}
	}

	override fun getItemCount(): Int {
		return filteredItems.size
	}

	inner class ItemFilter : Filter() {
		override fun performFiltering(filterText: CharSequence?): FilterResults {
			val filterResults = FilterResults()
			filterText?.let { text: CharSequence ->
				if (text.length > 0) {
					val list = _items.filter { getFilteredProperties(it).any { it.contains(text, true) } }
					filterResults.values = list
					filterResults.count = list.size
				}
			}
			return filterResults
		}

		override fun publishResults(filterText: CharSequence?, filterResults: FilterResults?) {
			if (filterResults?.values != null) {
				filteredItems = filterResults.values as MutableList<T>
			} else {
				filteredItems = _items
			}
			notifyDataSetChanged()
		}

	}
}
