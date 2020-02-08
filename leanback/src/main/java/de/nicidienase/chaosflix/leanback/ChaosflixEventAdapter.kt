package de.nicidienase.chaosflix.leanback

import android.support.v17.leanback.widget.ArrayObjectAdapter
import android.support.v17.leanback.widget.Presenter
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event

class ChaosflixEventAdapter(presenter: Presenter) : ArrayObjectAdapter(presenter) {

    init {
        setHasStableIds(true)
    }

    override fun getId(position: Int): Long {
        val item = get(position)
        return if (item is Event) {
            item.id
        } else {
            -1
        }
    }
}
