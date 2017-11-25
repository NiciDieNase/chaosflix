package de.nicidienase.chaosflix.touch.eventdetails

import android.Manifest
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentRecording
import de.nicidienase.chaosflix.touch.OnEventSelectedListener
import de.nicidienase.chaosflix.touch.ViewModelFactory
import de.nicidienase.chaosflix.touch.playback.PlayerActivity

class EventDetailsActivity : AppCompatActivity(),
		EventDetailsFragment.OnEventDetailsFragmentInteractionListener,
		OnEventSelectedListener {

	override fun onEventSelected(event: PersistentEvent, v: View) {
		showFragmentForEvent(event.eventId, true)
	}

	private lateinit var viewModel: DetailsViewModel

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_eventdetails)
        viewModel = ViewModelProviders.of(this, ViewModelFactory).get(DetailsViewModel::class.java)
		viewModel.writeExternalStorageAllowed = hasWriteStoragePermission()

		val eventId = intent.getLongExtra(EXTRA_EVENT, 0)

		showFragmentForEvent(eventId)
		requestWriteStoragePermission()
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

	override fun onToolbarStateChange() {
		invalidateOptionsMenu()
	}

	override fun playItem(event: PersistentEvent, recording: PersistentRecording) {
		PlayerActivity.launch(this, event, recording)
	}

	companion object {
		private val EXTRA_EVENT = "extra_event"

		fun launch(context: Context, eventId: Long) {
			val intent = Intent(context, EventDetailsActivity::class.java)
			intent.putExtra(EXTRA_EVENT, eventId)
			context.startActivity(intent)
		}
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if(requestCode == PERMISSION_REQUEST_CODE && grantResults.size > 0){
			if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
				requestWriteStoragePermission()
			} else {
				viewModel.writeExternalStorageAllowed = true
			}
		}
	}

	private val PERMISSION_REQUEST_CODE: Int = 1;
	private fun requestWriteStoragePermission() {
		ActivityCompat.requestPermissions(this,
				arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE);}

	private fun hasWriteStoragePermission(): Boolean {
		return ActivityCompat.checkSelfPermission(
				this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;}

}