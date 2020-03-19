package de.nicidienase.chaosflix.touch.playback

import android.util.Log
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray

/**
 * Created by felix on 27.09.17.
 */
internal class PlayerEventListener(private val player: SimpleExoPlayer, private val listener: PlayerStateChangeListener) : Player.EventListener, SimpleExoPlayer.VideoListener {
    override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {}
    override fun onLoadingChanged(isLoading: Boolean) {
        if (isLoading && player.playbackState != Player.STATE_READY) {
            listener.notifyLoadingStart()
        } else {
            listener.notifyLoadingFinished()
        }
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            Player.STATE_BUFFERING -> if (player.isLoading) {
                listener.notifyLoadingStart()
            }
            Player.STATE_ENDED -> {
                Log.d(TAG, "Finished Playback")
                listener.notifyEnd()
            }
            Player.STATE_IDLE, Player.STATE_READY -> listener.notifyLoadingFinished()
            else -> listener.notifyLoadingFinished()
        }
    }

    override fun onRepeatModeChanged(repeatMode: Int) {}
    override fun onPlayerError(error: ExoPlaybackException) {
        val errorMessage = error.cause!!.message
        listener.notifyError(errorMessage)
        Log.d(TAG, errorMessage, error)
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}
    override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {}
    override fun onRenderedFirstFrame() {
        listener.notifyLoadingFinished()
    }

    interface PlayerStateChangeListener {
        fun notifyLoadingStart()
        fun notifyLoadingFinished()
        fun notifyError(errorMessage: String?)
        fun notifyEnd()
    }

    companion object {
        private val TAG = PlayerEventListener::class.java.simpleName
    }
}
