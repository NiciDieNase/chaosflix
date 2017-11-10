package de.nicidienase.chaosflix.touch.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import de.nicidienase.chaosflix.R
import java.util.*

abstract class ItemRecyclerViewAdapter<T>()
    : RecyclerView.Adapter<ItemRecyclerViewAdapter<T>.ViewHolder>(), Filterable {

    internal abstract val layout: Int

    internal abstract fun getFilteredProperty(item: T): String

    private val filter by lazy { ItemFilter()}

    override fun getFilter(): Filter {
        return filter
    }

    private var _items: MutableList<T> = ArrayList<T>()
    private var filteredItems: MutableList<T> = _items

    var items: MutableList<T>
        get() = filteredItems
        set(value) {
            _items = value
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return filteredItems.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        var mItem: T? = null
        val mIcon: ImageView
        val mTitleText: TextView
        val mSubtitle: TextView
        val mTag: TextView

        init {
            mIcon = mView.findViewById<View>(R.id.imageView) as ImageView
            mTitleText = mView.findViewById<View>(R.id.title_text) as TextView
            mSubtitle = mView.findViewById<View>(R.id.acronym_text) as TextView
            mTag = mView.findViewById<View>(R.id.tag_text) as TextView
        }
    }

    inner class ItemFilter: Filter(){
        override fun performFiltering(filterText: CharSequence?): FilterResults {
            val filterResults = FilterResults()
            filterText?.let { text: CharSequence ->
                if(text.length > 0){
                    val list = items.filter { getFilteredProperty(it).contains(text, true) }
                    filterResults.values = list
                    filterResults.count = list.size
                }
            }
            return filterResults
        }

        override fun publishResults(filterText: CharSequence?, filterResults: FilterResults?) {
            filteredItems = filterResults?.values as MutableList<T>
            notifyDataSetChanged()
        }

    }
}
