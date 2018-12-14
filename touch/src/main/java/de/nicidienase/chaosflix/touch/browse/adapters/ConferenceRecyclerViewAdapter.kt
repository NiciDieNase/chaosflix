package de.nicidienase.chaosflix.touch.browse.adapters

import com.squareup.picasso.Picasso
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.touch.browse.ConferencesTabBrowseFragment
import java.util.*

class ConferenceRecyclerViewAdapter(private val mListener: ConferencesTabBrowseFragment.OnInteractionListener?) : ItemRecyclerViewAdapter<Conference>() {
	override fun getFilteredProperties(item: Conference): List<String> {
		return listOf(item.title)
	}

	override val layout = R.layout.item_conference_cardview

	override fun getComparator(): Comparator<in Conference>? {
//        return Comparator { o1, o2 -> o1.acronym.compareTo(o2.acronym) * -1 }
		return null
	}

	override fun getItemId(position: Int): Long {
		return items.get(position).id
	}

	override fun onBindViewHolder(holder: ItemRecyclerViewAdapter<Conference>.ViewHolder, position: Int) {
		holder.titleText.setText(items[position].title)
		holder.subtitle.setText(items[position].acronym)
		Picasso.with(holder.icon.context)
				.load(items[position].logoUrl)
				.fit()
				.centerInside()
				.into(holder.icon)

		holder.mView.setOnClickListener { _ ->
			mListener?.onConferenceSelected((items[position]))
		}
	}
}
