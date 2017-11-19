package de.nicidienase.chaosflix.touch.browse

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.transition.Slide
import android.transition.TransitionInflater
import android.view.Gravity
import android.view.View

import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.touch.browse.eventslist.EventsListFragment
import de.nicidienase.chaosflix.touch.eventdetails.EventDetailsActivity

abstract class BrowseBaseActivity : AppCompatActivity(), EventsListFragment.OnInteractionListener {
    protected val numColumns: Int
        get() = resources.getInteger(R.integer.num_columns)

    protected fun showFragment(fragment: Fragment) {
        val fm = supportFragmentManager
        val oldFragment = fm.findFragmentById(R.id.fragment_container)

        val transitionInflater = TransitionInflater.from(this)
        if (oldFragment != null) {
            if(fragment::class == oldFragment::class){
                return
            }
            oldFragment.exitTransition = transitionInflater.inflateTransition(android.R.transition.fade)
        }
        fragment.enterTransition = transitionInflater.inflateTransition(android.R.transition.fade)

//        val slideTransition = Slide(Gravity.RIGHT)
//        fragment.enterTransition = slideTransition

        val ft = fm.beginTransaction()
        ft.replace(R.id.fragment_container, fragment)
        ft.setReorderingAllowed(true)
        ft.addToBackStack(null)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        ft.commit()
    }

    override fun onEventSelected(event: PersistentEvent, v: View) {
        EventDetailsActivity.launch(this, event.eventId)
    }
}
