package de.nicidienase.chaosflix.touch.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import de.nicidienase.chaosflix.R
import java.util.*

abstract class ItemRecyclerViewAdapter<T>()
    : RecyclerView.Adapter<ItemRecyclerViewAdapter.ViewHolder<T>>() {

    internal abstract val layout: Int
    //    internal abstract val filteredProperty: String
    private var _items: MutableList<T> = ArrayList<T>()

    var items: MutableList<T>
        get() = _items
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
        val view = LayoutInflater.from(parent.context)
                .inflate(layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder<T>(val mView: View) : RecyclerView.ViewHolder(mView) {
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

        override fun toString(): String {
            return "super.toString() '$mTitleText.text'"
        }
    }
}
