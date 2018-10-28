package de.nicidienase.chaosflix.leanback.fragments

import android.content.Context
import android.support.v17.leanback.media.PlaybackTransportControlGlue
import android.support.v17.leanback.widget.Action
import android.support.v17.leanback.widget.ArrayObjectAdapter
import android.support.v17.leanback.widget.PlaybackControlsRow
import com.google.android.exoplayer2.ext.leanback.LeanbackPlayerAdapter
import java.util.concurrent.TimeUnit

class ChaosMediaPlayerGlue(context: Context,
                           playerAdapter: LeanbackPlayerAdapter) :
		PlaybackTransportControlGlue<LeanbackPlayerAdapter>(context, playerAdapter) {

	private val mSkipPreviousAction = PlaybackControlsRow.SkipPreviousAction(context)
	private val mSkipNextAction = PlaybackControlsRow.SkipNextAction(context)
	private val mFastForwardAction = PlaybackControlsRow.FastForwardAction(context)
	private val mRewindAction = PlaybackControlsRow.RewindAction(context)
	private val mRepeatAction = PlaybackControlsRow.RepeatAction(context)
	private val mThumbsUpAction = PlaybackControlsRow.ThumbsUpAction(context)
	private val mThumbsDownAction = PlaybackControlsRow.ThumbsDownAction(context)

	init {
		mThumbsUpAction.index = PlaybackControlsRow.ThumbsUpAction.INDEX_OUTLINE
		mThumbsDownAction.index = PlaybackControlsRow.ThumbsDownAction.INDEX_OUTLINE
	}

	override fun onCreatePrimaryActions(adapter: ArrayObjectAdapter) {
		// Order matters, super.onCreatePrimaryActions() will create the play / pause action.
		// Will display as follows:
		// play/pause, previous, rewind, fast forward, next
		//   > /||      |<        <<        >>         >|
		super.onCreatePrimaryActions(adapter)
		adapter.add(mSkipPreviousAction)
		adapter.add(mRewindAction)
		adapter.add(mFastForwardAction)
		adapter.add(mSkipNextAction)
	}

	override fun onCreateSecondaryActions(adapter: ArrayObjectAdapter) {
		super.onCreateSecondaryActions(adapter)
//		adapter.add(mThumbsDownAction)
		adapter.add(mThumbsUpAction)
//		adapter.add(mRepeatAction)
	}

	override fun onActionClicked(action: Action) {
		if (shouldDispatchAction(action)) {
			dispatchAction(action)
			return
		}
		// Super class handles play/pause and delegates to abstract methods next()/previous().
		super.onActionClicked(action)
	}

	// Should dispatch actions that the super class does not supply callbacks for.
	private fun shouldDispatchAction(action: Action): Boolean {
		return (action === mRewindAction
				|| action === mFastForwardAction
//				|| action === mRepeatAction
//				|| action === mThumbsDownAction
				|| action === mThumbsUpAction)
	}

	private fun dispatchAction(action: Action) {
		// Primary actions are handled manually.
		if (action === mRewindAction) {
			rewind()
		} else if (action === mFastForwardAction) {
			fastForward()
		} else if (action === mThumbsUpAction){
		} else if (action is PlaybackControlsRow.MultiAction) {
			action.nextIndex()
			// Notify adapter of action changes to handle secondary actions, such as, thumbs up/down
			// and repeat.
			notifyActionChanged(
					action,
					controlsRow.secondaryActionsAdapter as ArrayObjectAdapter)
		}
	}

	private fun notifyActionChanged(
			action: PlaybackControlsRow.MultiAction, adapter: ArrayObjectAdapter?) {
		if (adapter != null) {
			val index = adapter.indexOf(action)
			if (index >= 0) {
				adapter.notifyArrayItemRangeChanged(index, 1)
			}
		}
	}

	/** Skips backwards 10 seconds.  */
	fun rewind() {
		var newPosition = currentPosition - THIRTY_SECONDS
		newPosition = if (newPosition < 0) 0 else newPosition
		playerAdapter.seekTo(newPosition)
	}

	/** Skips forward 10 seconds.  */
	fun fastForward() {
		if (duration > -1) {
			var newPosition = currentPosition + THIRTY_SECONDS
			newPosition = if (newPosition > duration) duration else newPosition
			playerAdapter.seekTo(newPosition)
		}
	}

	companion object {
		private val THIRTY_SECONDS = TimeUnit.SECONDS.toMillis(30)

	}
}
