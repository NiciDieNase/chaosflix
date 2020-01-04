package de.nicidienase.chaosflix.touch.favoritesimport

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import de.nicidienase.chaosflix.common.viewmodel.FavoritesImportViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.touch.browse.adapters.ImportItemAdapter
import de.nicidienase.chaosflix.touch.databinding.FragmentFavoritesImportBinding

class FavoritesImportFragment : Fragment() {

    private lateinit var viewModel: FavoritesImportViewModel
    private lateinit var adapter: ImportItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val binding = FragmentFavoritesImportBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        viewModel = ViewModelProviders.of(this, ViewModelFactory.getInstance(requireContext())).get(FavoritesImportViewModel::class.java)

        binding.importList.layoutManager = LinearLayoutManager(context)
        adapter = ImportItemAdapter()
        binding.importList.adapter = adapter

        viewModel.items.observe(this, Observer {events ->
            adapter.submitList(events)
            adapter.notifyDataSetChanged()
        })

        viewModel.state.observe(this, Observer {
            val errorMessage = it.error
            when (it.state) {
                FavoritesImportViewModel.State.EVENTS_FOUND -> {
                    binding.incOverlay.loadingOverlay.visibility = View.GONE
                }
                FavoritesImportViewModel.State.WORKING -> {
                    binding.incOverlay.loadingOverlay.visibility = View.VISIBLE
                }
                FavoritesImportViewModel.State.ERROR -> {
                    Log.e(TAG, "Error $errorMessage")
                }
                FavoritesImportViewModel.State.IMPORT_DONE -> {
                    // TODO navigate to favorites
                    activity?.finish()
                }
            }

            if(errorMessage != null) {
                Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_SHORT).show()
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.import_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_import -> {
                viewModel.import()
                true
            }
            R.id.menu_item_select_all -> {
                viewModel.selectAll()
                true
            }
            else -> super.onOptionsItemSelected(item)
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