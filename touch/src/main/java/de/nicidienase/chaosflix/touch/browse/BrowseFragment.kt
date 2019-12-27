package de.nicidienase.chaosflix.touch.browse

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.View
import de.nicidienase.chaosflix.common.viewmodel.BrowseViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory

open class BrowseFragment : androidx.fragment.app.Fragment() {

    lateinit var viewModel: BrowseViewModel
    var overlay: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!, ViewModelFactory.getInstance(requireContext())).get(BrowseViewModel::class.java)
    }

    @JvmOverloads
    protected fun setupToolbar(toolbar: Toolbar, title: Int, isRoot: Boolean = true) {
        setupToolbar(toolbar, resources.getString(title), isRoot)
    }

    @JvmOverloads
    protected fun setupToolbar(toolbar: Toolbar, title: String, isRoot: Boolean = true) {
        val activity = activity as AppCompatActivity
        if (activity is BrowseActivity) {
            activity.setupDrawerToggle(toolbar)
        }
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.setTitle(title)
        if (isRoot) {
            activity.supportActionBar?.setDisplayShowHomeEnabled(true)
        } else {
            activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    protected fun setLoadingOverlayVisibility(visible: Boolean) {
        if (visible) {
            overlay?.setVisibility(View.VISIBLE)
        } else {
            overlay?.setVisibility(View.INVISIBLE)
        }
    }
}
