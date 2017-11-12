package de.nicidienase.chaosflix.touch.browse.adapters

import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.touch.browse.EventsListFragment

class RelatedEventsRecyclerViewAdapter(listener: EventsListFragment.OnEventsListFragmentInteractionListener): EventRecyclerViewAdapter(listener){
    override val layout = R.layout.related_event_cardview_layout
}