package de.nicidienase.chaosflix.touch.browse.adapters

import androidx.recyclerview.widget.RecyclerView
import android.widget.Filter
import android.widget.Filterable
import java.util.Collections

abstract class ItemRecyclerViewAdapter<T, VH : RecyclerView.ViewHolder?>() :
    RecyclerView.Adapter<VH>(), Filterable {

    abstract fun getComparator(): Comparator<in T>?

    internal abstract fun getFilteredProperties(item: T): List<String>

    private val _filter by lazy { ItemFilter() }

    override fun getFilter(): Filter {
        return _filter
    }

    private var _items: List<T> = listOf()

    private var filteredItems: List<T> = _items

    var items: List<T>
        get() = filteredItems
        set(value) {
            _items = value
            if (getComparator() != null) {
                Collections.sort(_items, getComparator())
            }
            filteredItems = _items
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int {
        return filteredItems.size
    }

    inner class ItemFilter : Filter() {
        override fun performFiltering(filterText: CharSequence?): FilterResults {
            val filterResults = FilterResults()
            filterText?.let { text: CharSequence ->
                if (text.isNotEmpty()) {
                    val list = _items.filter { getFilteredProperties(it).any { it.contains(text, true) } }
                    filterResults.values = list
                    filterResults.count = list.size
                }
            }
            return filterResults
        }

        override fun publishResults(filterText: CharSequence?, filterResults: FilterResults?) {
            val results = filterResults?.values
            if (results != null && results is List<*>) {
                filteredItems = results as List<T>
            } else {
                filteredItems = _items
            }
            notifyDataSetChanged()
        }
    }
}
