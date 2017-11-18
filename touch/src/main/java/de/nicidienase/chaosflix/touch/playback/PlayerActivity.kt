package de.nicidienase.chaosflix.touch.playback

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentRecording

class PlayerActivity : AppCompatActivity(), ExoPlayerFragment.OnMediaPlayerInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val event = intent.extras!!.getParcelable<PersistentEvent>(EVENT_KEY)
        val recording = intent.extras!!.getParcelable<PersistentRecording>(RECORDING_KEY)

        if (savedInstanceState == null) {
            val ft = supportFragmentManager.beginTransaction()
            val playerFragment = ExoPlayerFragment.newInstance(event, recording)
            ft.replace(R.id.fragment_container, playerFragment)
            ft.commit()
        }
    }

    companion object {
        val EVENT_KEY = "event"
        val RECORDING_KEY = "recording"
    }

}
