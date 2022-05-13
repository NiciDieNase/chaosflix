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
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import de.nicidienase.chaosflix.common.ImportItem
import de.nicidienase.chaosflix.common.viewmodel.FavoritesImportViewModel
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.touch.browse.adapters.ImportItemAdapter
import de.nicidienase.chaosflix.touch.databinding.FragmentFavoritesImportBinding
import org.koin.android.viewmodel.ext.android.viewModel

class FavoritesImportFragment : Fragment() {

    private val favoritesImportViewModel: FavoritesImportViewModel by viewModel()
    private lateinit var adapter: ImportItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val binding = FragmentFavoritesImportBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = favoritesImportViewModel

        binding.importList.layoutManager = LinearLayoutManager(context)
        val onItemClick: (ImportItem) -> Unit = {
            favoritesImportViewModel.itemChanged(it)
        }
        val onLectureClick: (ImportItem) -> Unit = {
            favoritesImportViewModel.unavailableItemClicked(it)
        }
        adapter = ImportItemAdapter(onListItemClick = onItemClick, onLectureClick = onLectureClick)
        adapter.setHasStableIds(true)
        binding.importList.setHasFixedSize(true)
        binding.importList.adapter = adapter

        favoritesImportViewModel.items.observe(viewLifecycleOwner, Observer { events ->
            if (events != null) {
                adapter.submitList(events.toList())
            }
        })

        favoritesImportViewModel.selectAll.observe(viewLifecycleOwner, Observer {
            activity?.invalidateOptionsMenu()
        })

        favoritesImportViewModel.state.observe(viewLifecycleOwner, Observer {
            when (it.state) {
                FavoritesImportViewModel.State.IMPORT_DONE -> {
                    // TODO navigate to favorites
                    activity?.finish()
                }
            }
        })

        favoritesImportViewModel.working.observe(viewLifecycleOwner, Observer { working ->
            binding.incOverlay.loadingOverlay.visibility = if (working) View.VISIBLE else View.GONE
        })
        favoritesImportViewModel.processCount.observe(viewLifecycleOwner, Observer { pair ->
            Log.i(TAG, "Progress ${pair.first}/${pair.second}")
            binding.incOverlay.progressbar.apply {
                if (pair.second != 0) {
                    visibility = View.VISIBLE
                    progress = (100 * pair.first / pair.second)
                }
            }
        })

        favoritesImportViewModel.errorMessage.observe(viewLifecycleOwner, Observer { errorMessage ->
            if (errorMessage != null) {
                Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).apply {
                    view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).maxLines = 5
                    setAction("OK", View.OnClickListener {
                        this.dismiss()
                    }).show()
                }
                favoritesImportViewModel.errorShown()
            }
        })

        favoritesImportViewModel.importItemCount.observe(viewLifecycleOwner, Observer { count ->
            if (count == 0) {
                binding.buttonImport.hide()
            } else {
                binding.buttonImport.show()
            }
        })

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

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.import_menu, menu)
        if (favoritesImportViewModel.selectAll.value != false) {
            menu.removeItem(R.id.menu_item_unselect_all)
        } else {
            menu.removeItem(R.id.menu_item_select_all)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_select_all -> {
                favoritesImportViewModel.selectAll()
                favoritesImportViewModel.selectAll.value = false
                true
            }
            R.id.menu_item_unselect_all -> {
                favoritesImportViewModel.selectNone()
                favoritesImportViewModel.selectAll.value = true
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handleJson(intent: Intent) {
        val extra = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (extra?.isNotEmpty() == true) {
            favoritesImportViewModel.handleLectures(extra)
        }
    }

    companion object {
        private val TAG = FavoritesImportFragment::class.java.simpleName
    }
}
