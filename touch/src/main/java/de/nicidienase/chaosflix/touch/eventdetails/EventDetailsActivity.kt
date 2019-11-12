package de.nicidienase.chaosflix.touch.eventdetails

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import de.nicidienase.chaosflix.common.ChaosflixUtil
import de.nicidienase.chaosflix.common.OfflineItemManager
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import de.nicidienase.chaosflix.common.viewmodel.DetailsViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.touch.OnEventSelectedListener
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.touch.browse.cast.CastService
import de.nicidienase.chaosflix.touch.playback.PlayerActivity
import kotlinx.android.synthetic.main.activity_eventdetails.*

class EventDetailsActivity : AppCompatActivity(),
        EventDetailsFragment.OnEventDetailsFragmentInteractionListener,
        OnEventSelectedListener {
    private lateinit var viewModel: DetailsViewModel

    private lateinit var castService: CastService

    private var selectDialog: AlertDialog? = null
    private var pendingDownload: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eventdetails)

        castService = CastService(this)

        viewModel = ViewModelProviders.of(this, ViewModelFactory(this)).get(DetailsViewModel::class.java)

        viewModel.state.observe(this, Observer { liveEvent ->
            if (liveEvent == null) {
                return@Observer
            }
            val event = liveEvent.data?.getParcelable<Event>(DetailsViewModel.EVENT)
            val recording = liveEvent.data?.getParcelable<Recording>(DetailsViewModel.RECORDING)
            val localFile = liveEvent.data?.getString(DetailsViewModel.KEY_LOCAL_PATH)
            val selectItems: Array<Recording>? = liveEvent.data?.getParcelableArray(DetailsViewModel.KEY_SELECT_RECORDINGS) as Array<Recording>?
            when (liveEvent.state) {
                DetailsViewModel.State.DisplayEvent -> {
                    if (event != null) {
                        showFragmentForEvent(event, true)
                    }
                }
                DetailsViewModel.State.PlayOfflineItem -> {
                    if (event != null && recording != null) {
                        playItem(event, recording, localFile)
                    }
                }
                DetailsViewModel.State.PlayOnlineItem -> {
                    if (event != null && recording != null) {
                        playItem(event, recording)
                    }
                }
                DetailsViewModel.State.SelectRecording -> {
                    if (event != null && selectItems != null) {
                            selectRecording(event, selectItems.asList()) { e, r ->
                                viewModel.playRecording(e, r)
                            }
                    }
                }
                DetailsViewModel.State.DownloadRecording -> {
                    if (event != null && selectItems != null) {
                        selectRecording(event, selectItems.asList()) { e, r ->
                            viewModel.download(e, r).observe(this, Observer {
                                when (it?.state) {
                                    OfflineItemManager.State.Downloading -> {
                                        Snackbar.make(fragment_container, "Download started", Snackbar.LENGTH_LONG).show()
                                    }
                                    OfflineItemManager.State.PermissionRequired -> {
                                        pendingDownload = liveEvent.data
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            this.requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                                    WRITE_PERMISSION_REQUEST)
                                        }
                                    }
                                    OfflineItemManager.State.Done -> {}
                                }
                            })
                        }
                    }
                }
                DetailsViewModel.State.PlayExternal -> {
                    if (event != null) {
                        if (selectItems != null) {
                            selectRecording(event, selectItems.asList()) { _, r ->
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(r.recordingUrl)))
                            }
                        } else if (recording != null) {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(recording.recordingUrl)))
                        }
                    }
                }
                DetailsViewModel.State.Error -> {
                    showSnackbar(liveEvent.error ?: "An Error occured")
                }
            }
        })

        val event = intent.getParcelableExtra<Event>(EXTRA_EVENT)

        showFragmentForEvent(event)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == WRITE_PERMISSION_REQUEST) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                pendingDownload?.let {
                    val recording = it.getParcelable<Recording>(DetailsViewModel.RECORDING)
                    val event = it.getParcelable<Event>(DetailsViewModel.EVENT)
                    if (event != null && recording != null) {
                        viewModel.download(event, recording)
                    }
                }
                pendingDownload = null
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun showSnackbar(message: String, duration: Int = Snackbar.LENGTH_LONG) {
        Snackbar.make(fragment_container, message, duration)
    }

    private fun selectRecording(event: Event, recordings: List<Recording>, action: (Event, Recording) -> Unit) {
        val optimalRecording = ChaosflixUtil.getOptimalRecording(recordings, event.originalLanguage)
        if (optimalRecording != null && viewModel.autoselectRecording) {
            action.invoke(event, optimalRecording)
        } else {
            val items: List<String> = recordings.map { ChaosflixUtil.getStringForRecording(it) }
            selectRecordingFromList(items, DialogInterface.OnClickListener { _, i ->
                action.invoke(event, recordings[i])
            })
        }
    }

    private fun selectRecordingFromList(items: List<String>, resultHandler: DialogInterface.OnClickListener) {
            if (selectDialog != null) {
                selectDialog?.dismiss()
            }
            val builder = AlertDialog.Builder(this)
            builder.setItems(items.toTypedArray(), resultHandler)
            selectDialog = builder.create()
            selectDialog?.show()
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

    private fun playItem(event: Event, recording: Recording, localFile: String? = null) {
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menu?.let {
            castService.addMediaRouteMenuItem(it)
        }
        return true
    }

    companion object {

        private const val EXTRA_EVENT = "extra_event"
        private const val EXTRA_URI = "extra_uri"
        private const val EXTRA_GUID = "extra_guid"
        private const val WRITE_PERMISSION_REQUEST = 23
        private val TAG = EventDetailsActivity::class.java.simpleName

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
