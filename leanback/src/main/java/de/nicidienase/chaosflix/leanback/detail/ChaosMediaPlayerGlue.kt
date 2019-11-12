package de.nicidienase.chaosflix.leanback.detail

import android.content.Context
import android.support.v17.leanback.media.PlaybackTransportControlGlue
import android.support.v17.leanback.widget.Action
import android.support.v17.leanback.widget.ArrayObjectAdapter
import android.support.v17.leanback.widget.PlaybackControlsRow
import com.google.android.exoplayer2.ext.leanback.LeanbackPlayerAdapter
import java.util.concurrent.TimeUnit

class ChaosMediaPlayerGlue(context: Context, playerAdapter: LeanbackPlayerAdapter) :
        PlaybackTransportControlGlue<LeanbackPlayerAdapter>(context, playerAdapter) {

    private val mThumbsUpAction = PlaybackControlsRow.ThumbsUpAction(context)

    init {
        mThumbsUpAction.index = PlaybackControlsRow.ThumbsUpAction.INDEX_OUTLINE
    }

    override fun onCreateSecondaryActions(adapter: ArrayObjectAdapter) {
        super.onCreateSecondaryActions(adapter)
        adapter.add(mThumbsUpAction)
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
        return (action === mThumbsUpAction)
    }

    private fun dispatchAction(action: Action) {
        // Primary actions are handled manually.
        if (action === mThumbsUpAction) {
            // TODO create watchlist entry
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
        action: PlaybackControlsRow.MultiAction,
        adapter: ArrayObjectAdapter?
    ) {
        if (adapter != null) {
            val index = adapter.indexOf(action)
            if (index >= 0) {
                adapter.notifyArrayItemRangeChanged(index, 1)
            }
        }
    }

    companion object {
        private val THIRTY_SECONDS = TimeUnit.SECONDS.toMillis(30)
    }
}
