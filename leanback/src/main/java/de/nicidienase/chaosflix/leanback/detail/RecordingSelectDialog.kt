package de.nicidienase.chaosflix.leanback.detail

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist.Guidance
import androidx.leanback.widget.GuidedAction
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import de.nicidienase.chaosflix.common.util.RecordingUtil
import de.nicidienase.chaosflix.leanback.R

class RecordingSelectDialog private constructor() : GuidedStepSupportFragment() {

    private lateinit var recordings: List<Recording>

    private var onRecordingSelected: ((Recording) -> Unit?)? = null

    override fun onCreateGuidance(savedInstanceState: Bundle?): Guidance {
        return Guidance("Select Recording", "", null, null)
    }

    override fun onCreateActions(actions: MutableList<GuidedAction?>, savedInstanceState: Bundle?) {
        recordings.forEach {
            val action = GuidedAction.Builder(requireContext())
                    .id(it.id)
                    .title(RecordingUtil.getStringForRecording(it))
                    .build()
            actions.add(action)
        }
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        val recording = recordings.first { it.id == action.id }
        onRecordingSelected?.invoke(recording)
        finishGuidedStepSupportFragments()
    }

    fun show(fragmentManager: FragmentManager) {
        add(fragmentManager, this, R.id.details_fragment)
    }

    companion object {
        const val RECORDINGS = "recordings"

        fun create(recordings: List<Recording>, onRecordingSelected: (Recording) -> Unit?): RecordingSelectDialog {
            val recordingSelectDialog = RecordingSelectDialog()
            recordingSelectDialog.recordings = recordings
            recordingSelectDialog.onRecordingSelected = onRecordingSelected
            return recordingSelectDialog
        }
    }
}
