package de.nicidienase.chaosflix.touch.browse.adapters

import android.support.v4.view.ViewCompat
import android.view.View

import com.squareup.picasso.Picasso

import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.touch.OnEventSelectedListener
import java.util.Comparator

open class EventRecyclerViewAdapter(val listener: OnEventSelectedListener) :
        ItemRecyclerViewAdapter<PersistentEvent>() {

    override fun getComparator(): Comparator<in PersistentEvent>? {
        return Comparator { o1, o2 -> o1.title.compareTo(o2.title) }
    }

    override fun getFilteredProperties(item: PersistentEvent): List<String> {
        return listOf(item.title,
                item.subtitle,
                item.description,
                item.getSpeakerString()
                ).filterNotNull()
    }


    override val layout = R.layout.item_event_cardview
    var showTags: Boolean = false

    override fun onBindViewHolder(holder: ItemRecyclerViewAdapter<PersistentEvent>.ViewHolder, position: Int) {
        val event = items[position]

        holder.mTitleText.text = event.title
        holder.mSubtitle.text = event.subtitle
        if (showTags) {
            val tagString = StringBuilder()
            for (tag in event.tags!!) {
                if (tagString.length > 0) {
                    tagString.append(", ")
                }
                tagString.append(tag)
            }
            holder.mTag.text = tagString
        }
        Picasso.with(holder.mIcon.context)
                .load(event.thumbUrl)
                .noFade()
                .fit()
                .centerInside()
                .into(holder.mIcon)

        val resources = holder.mTitleText.context.getResources()
        ViewCompat.setTransitionName(holder.mTitleText,
                resources.getString(R.string.title) + event.eventId)
        ViewCompat.setTransitionName(holder.mSubtitle,
                resources.getString(R.string.subtitle) + event.eventId)
        ViewCompat.setTransitionName(holder.mIcon,
                resources.getString(R.string.thumbnail) + event.eventId)

        holder.mView.setOnClickListener({ v: View -> listener.onEventSelected(items[position], v) })
    }
}
