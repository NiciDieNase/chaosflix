package de.nicidienase.chaosflix.touch

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.viewmodel.SplashViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.touch.browse.eventslist.EventsListActivity
import de.nicidienase.chaosflix.touch.eventdetails.EventDetailsActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (intent.action) {
            Intent.ACTION_VIEW -> {
                setContentView(R.layout.activity_splash)
                handleViewAction(intent.data)
            }
            else -> {
                goToOverview()
            }
        }
    }

    private fun handleViewAction(data: Uri?) {
        val viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory.getInstance(this)
        )[SplashViewModel::class.java]

        viewModel.state.observe(this, Observer {
            when (it.state) {
                SplashViewModel.State.FOUND -> {
                    when (val item = it.data) {
                        is Event -> goToEvent(item)
                        is Conference -> goToConference(item)
                        else -> goToOverview()
                    }
                }
                SplashViewModel.State.NOT_FOUND -> {
                    goToOverview()
                }
            }
        })

        if (data != null) {
            when (data.pathSegments[0]) {
                "v" -> viewModel.findEventForUri(data)
                "c" -> viewModel.findConferenceForUri(data)
            }
        } else {
            goToOverview()
        }
    }

    private fun goToOverview() {
        NavigationActivity.launch(this)
        finish()
    }

    private fun goToEvent(event: Event) {
        EventDetailsActivity.launch(this, event)
        finish()
    }

    private fun goToConference(conference: Conference) {
        EventsListActivity.start(this, conference)
        finish()
    }
}
