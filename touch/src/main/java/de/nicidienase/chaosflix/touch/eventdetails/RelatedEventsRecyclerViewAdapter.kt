package de.nicidienase.chaosflix.touch.eventdetails

import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.touch.OnEventSelectedListener
import de.nicidienase.chaosflix.touch.browse.adapters.EventRecyclerViewAdapter

class RelatedEventsRecyclerViewAdapter(listener: OnEventSelectedListener) : EventRecyclerViewAdapter(listener) {
	override val layout = R.layout.related_event_cardview_layout
}