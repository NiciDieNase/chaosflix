package de.nicidienase.chaosflix.touch.browse.download

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.entities.streaming.LiveConference
import de.nicidienase.chaosflix.common.entities.streaming.Stream
import de.nicidienase.chaosflix.databinding.FragmentDownloadsBinding
import de.nicidienase.chaosflix.databinding.FragmentLivestreamsBinding
import de.nicidienase.chaosflix.touch.browse.BrowseFragment

class DownloadsListFragment : BrowseFragment(){

    private lateinit var listener: InteractionListener
    private lateinit var binding: FragmentDownloadsBinding

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is InteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement LivestreamListFragment.InteractionListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDownloadsBinding.inflate(inflater, container, false)
        setupToolbar(binding.incToolbar?.toolbar!!, R.string.downloads)
        overlay = binding.incOverlay?.loadingOverlay
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLoadingOverlayVisibility(false)
    }

    interface InteractionListener{
        fun onStreamSelected(conference: LiveConference, stream: Stream)
    }
}