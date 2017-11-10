package de.nicidienase.chaosflix.touch.adapters

import android.support.v4.view.ViewCompat
import android.view.View

import com.squareup.picasso.Picasso

import java.util.Collections

import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.touch.fragments.EventsListFragment

class EventRecyclerViewAdapter(val listener: EventsListFragment.OnEventsListFragmentInteractionListener) :
        ItemRecyclerViewAdapter<PersistentEvent>() {
    override fun getFilteredProperty(item: PersistentEvent): String {
        return item.title
    }

    override val layout = R.layout.event_cardview_layout
    var showTags: Boolean = false

    init {
        Collections.sort(items) { o1, o2 -> o1.title.compareTo(o2.title) }

    }

    override fun onBindViewHolder(holder: ItemRecyclerViewAdapter<PersistentEvent>.ViewHolder, position: Int) {
        val event = items[position]

        holder.mItem = event
        holder.mTitleText.setText(event.title)
        holder.mSubtitle.setText(event.subtitle)
        if (showTags) {
            val tagString = StringBuilder()
            for (tag in event.tags!!) {
                if (tagString.length > 0) {
                    tagString.append(", ")
                }
                tagString.append(tag)
            }
            holder.mTag.setText(tagString)
        }
        Picasso.with(holder.mIcon.getContext())
                .load(event.thumbUrl)
                .noFade()
                .fit()
                .centerInside()
                .into(holder.mIcon)

        val resources = holder.mTitleText.getContext().getResources()
        ViewCompat.setTransitionName(holder.mTitleText,
                resources.getString(R.string.title) + event.eventId)
        ViewCompat.setTransitionName(holder.mSubtitle,
                resources.getString(R.string.subtitle) + event.eventId)
        ViewCompat.setTransitionName(holder.mIcon,
                resources.getString(R.string.thumbnail) + event.eventId)

        holder.mView.setOnClickListener({ v: View -> listener.onEventSelected(holder.mItem as PersistentEvent, v) })
    }
}
