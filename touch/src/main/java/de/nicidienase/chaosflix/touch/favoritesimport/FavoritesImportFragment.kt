package de.nicidienase.chaosflix.touch.favoritesimport

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import de.nicidienase.chaosflix.common.viewmodel.FavoritesImportViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.touch.browse.adapters.ImportItemAdapter
import de.nicidienase.chaosflix.touch.databinding.FragmentFavoritesImportBinding

class FavoritesImportFragment : Fragment() {

    private lateinit var viewModel: FavoritesImportViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentFavoritesImportBinding.inflate(inflater, container, false)
        binding.setLifecycleOwner(this)
        viewModel = ViewModelProviders.of(this, ViewModelFactory.getInstance(requireContext())).get(FavoritesImportViewModel::class.java)

        binding.importList.layoutManager = LinearLayoutManager(context)
        val adapter = ImportItemAdapter()
        binding.importList.adapter = adapter

        binding.importButton.setOnClickListener {
            viewModel.import(adapter.currentList)
        }

        viewModel.state.observe(this, Observer {
            when (it.state) {
                FavoritesImportViewModel.State.EVENTS_FOUND -> {
                    it.data?.let { events ->
                        adapter.submitList(events)
                        adapter.notifyDataSetChanged()
                    }
                    binding.incOverlay.loadingOverlay.visibility = View.GONE
                }
                FavoritesImportViewModel.State.WORKING -> {
                    binding.incOverlay.loadingOverlay.visibility = View.VISIBLE
                }
                FavoritesImportViewModel.State.ERROR -> {
                    Log.e(TAG, "Error", it.error)
                }
                FavoritesImportViewModel.State.IMPORT_DONE -> {
                    // TODO navigate to favorites
                    activity?.finish()
                }
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val intent = activity?.intent
        when {
            intent?.action == Intent.ACTION_SEND -> {
                when (intent.type) {
                    "text/json" -> handleJson(intent)
                }
            }
            else -> {
                // Handle other intents, such as being started from the home screen
            }
        }
    }

    private fun handleJson(intent: Intent) {
        val extra = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (extra.isNotEmpty()) {
            viewModel.handleLectures(extra)
        }
    }

    companion object {
        private val TAG = FavoritesImportFragment::class.java.simpleName
    }
}