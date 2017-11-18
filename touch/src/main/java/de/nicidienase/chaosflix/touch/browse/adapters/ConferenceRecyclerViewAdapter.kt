package de.nicidienase.chaosflix.touch.browse.adapters

import com.squareup.picasso.Picasso

import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentConference
import de.nicidienase.chaosflix.touch.browse.ConferencesTabBrowseFragment
import java.util.Comparator

class ConferenceRecyclerViewAdapter(private val mListener: ConferencesTabBrowseFragment.OnInteractionListener?) : ItemRecyclerViewAdapter<PersistentConference>() {
    override fun getFilteredProperties(item: PersistentConference): List<String> {
        return listOf(item.title)
    }

    override val layout = R.layout.item_conference_cardview

    override fun getComparator(): Comparator<in PersistentConference>? {
//        return Comparator { o1, o2 -> o1.acronym.compareTo(o2.acronym) * -1 }
        return null
    }

    override fun onBindViewHolder(holder: ItemRecyclerViewAdapter<PersistentConference>.ViewHolder, position: Int) {
        holder.mTitleText.setText(items[position].title)
        holder.mSubtitle.setText(items[position].acronym)
        Picasso.with(holder.mIcon.context)
                .load(items[position].logoUrl)
                .fit()
                .centerInside()
                .into(holder.mIcon)

        holder.mView.setOnClickListener { _ ->
            mListener?.onConferenceSelected((items[position]).conferenceId)
        }
    }
}
