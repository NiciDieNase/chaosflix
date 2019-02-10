package de.nicidienase.chaosflix.touch.eventdetails

import android.Manifest
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import de.nicidienase.chaosflix.common.viewmodel.DetailsViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.touch.OnEventSelectedListener
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.touch.browse.cast.CastService
import de.nicidienase.chaosflix.touch.playback.PlayerActivity

class EventDetailsActivity : AppCompatActivity(),
		EventDetailsFragment.OnEventDetailsFragmentInteractionListener,
		OnEventSelectedListener {
	private lateinit var viewModel: DetailsViewModel

	private val PERMISSION_REQUEST_CODE: Int = 1;

	private lateinit var castService: CastService

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_eventdetails)

		castService = CastService(this)

		viewModel = ViewModelProviders.of(this, ViewModelFactory(this)).get(DetailsViewModel::class.java)
		viewModel.writeExternalStorageAllowed = hasWriteStoragePermission()

		val event = intent.getParcelableExtra<Event>(EXTRA_EVENT)

		showFragmentForEvent(event)
		if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
						Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			requestWriteStoragePermission()
		}
	}

	private fun showFragmentForEvent(event: Event, addToBackStack: Boolean = false) {
		val detailsFragment = EventDetailsFragment.newInstance(event)

		detailsFragment.allowEnterTransitionOverlap = true
		detailsFragment.allowReturnTransitionOverlap = true

		val ft = supportFragmentManager.beginTransaction()
		ft.replace(R.id.fragment_container, detailsFragment)
		if (addToBackStack) {
			ft.addToBackStack(null)
		}
		ft.setReorderingAllowed(true)

		ft.commit()
	}

	override fun onEventSelected(event: Event) {
		showFragmentForEvent(event, true)
	}

	override fun onToolbarStateChange() {
		invalidateOptionsMenu()
	}

	override fun playItem(event: Event, recording: Recording, localFile: String?) {
		if (castService.connected) {
			castService.loadMediaAndPlay(recording, event)
		} else {
			if (localFile != null) {
				PlayerActivity.launch(this, event, localFile)
			} else {
				PlayerActivity.launch(this, event, recording)
			}
		}
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == PERMISSION_REQUEST_CODE && grantResults.size > 0) {
			if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//				requestWriteStoragePermission()
			} else {
				viewModel.writeExternalStorageAllowed = true
				invalidateOptionsMenu()
			}
		}
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		super.onCreateOptionsMenu(menu)
		menu?.let {
			castService.addMediaRouteMenuItem(it)
		}
		return true
	}

	private fun requestWriteStoragePermission() {
		ActivityCompat.requestPermissions(this,
				arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE);
	}

	private fun hasWriteStoragePermission(): Boolean {
		return ActivityCompat.checkSelfPermission(
				this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
	}

	companion object {

		private val EXTRA_EVENT = "extra_event"
		private val EXTRA_URI = "extra_uri"
		private val TAG = EventDetailsActivity.javaClass.simpleName

		fun launch(context: Context, event: Event) {
			val intent = Intent(context, EventDetailsActivity::class.java)
			intent.putExtra(EXTRA_EVENT, event)
			context.startActivity(intent)
		}

		fun launch(context: Context, eventId: Uri) {
			val intent = Intent(context, EventDetailsActivity::class.java)
			intent.putExtra(EXTRA_URI, eventId)
			context.startActivity(intent)
		}
	}

}
