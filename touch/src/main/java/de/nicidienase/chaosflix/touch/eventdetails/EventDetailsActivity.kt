package de.nicidienase.chaosflix.touch.eventdetails

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentRecording
import de.nicidienase.chaosflix.touch.OnEventSelectedListener
import de.nicidienase.chaosflix.touch.ViewModelFactory
import de.nicidienase.chaosflix.touch.playback.PlayerActivity
import de.nicidienase.chaosflix.touch.resolver.BrowseFilter
import java.net.URI

class EventDetailsActivity : AppCompatActivity(),
		EventDetailsFragment.OnEventDetailsFragmentInteractionListener,
		OnEventSelectedListener {
	private lateinit var viewModel: DetailsViewModel

	private val PERMISSION_REQUEST_CODE: Int = 1;

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_eventdetails)
		viewModel = ViewModelProviders.of(this, ViewModelFactory).get(DetailsViewModel::class.java)
		viewModel.writeExternalStorageAllowed = hasWriteStoragePermission()

		val eventId = intent.getLongExtra(EXTRA_EVENT, 0)

		showFragmentForEvent(eventId)
		if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
				Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			requestWriteStoragePermission()
		}
	}

	private fun showFragmentForEvent(eventId: Long, addToBackStack: Boolean = false) {
		val detailsFragment = EventDetailsFragment.newInstance(eventId)

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

	override fun onEventSelected(event: PersistentEvent) {
		showFragmentForEvent(event.eventId, true)
	}

	override fun onToolbarStateChange() {
		invalidateOptionsMenu()
	}

	override fun playItem(event: PersistentEvent, recording: PersistentRecording) {
		PlayerActivity.launch(this, event, recording)
	}

	override fun playItem(event: PersistentEvent, uri: String) {
		PlayerActivity.launch(this, event, uri)
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

		fun launch(context: Context, eventId: Long) {
			val intent = Intent(context, EventDetailsActivity::class.java)
			intent.putExtra(EXTRA_EVENT, eventId)
			context.startActivity(intent)
		}

		fun launch(context: BrowseFilter, eventId: Uri) {
			val intent = Intent(context, EventDetailsActivity::class.java)
			intent.putExtra(EXTRA_URI, eventId)
			context.startActivity(intent)
		}
	}

}
