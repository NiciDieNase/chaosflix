package de.nicidienase.chaosflix.touch

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.viewmodel.SplashViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.touch.browse.BrowseActivity
import de.nicidienase.chaosflix.touch.eventdetails.EventDetailsActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory.getInstance(this)
        )[SplashViewModel::class.java]

        viewModel.state.observe(this, Observer {
            when (it.state) {
                SplashViewModel.State.FOUND -> {
                    val event = it.data
                    if (event != null) {
                        goToEvent(event)
                    } else {
                        goToOverview()
                    }
                }
                SplashViewModel.State.NOT_FOUND -> {
                    goToOverview()
                }
            }
        })

        when (intent.action) {
            Intent.ACTION_VIEW -> {
                viewModel.findEventForUri(intent.data)
            }
            else -> {
                goToOverview()
            }
        }
    }

    private fun goToOverview() {
        BrowseActivity.launch(this)
        finish()
    }

    private fun goToEvent(event: Event) {
        EventDetailsActivity.launch(this, event)
        finish()
    }
}