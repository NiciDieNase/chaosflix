package de.nicidienase.chaosflix.touch.browse

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View

import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.touch.ViewModelFactory

open class BrowseFragment : Fragment() {

    lateinit var viewModel: BrowseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!, ViewModelFactory).get(BrowseViewModel::class.java)
    }
}
