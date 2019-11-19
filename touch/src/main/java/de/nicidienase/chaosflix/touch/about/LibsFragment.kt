package de.nicidienase.chaosflix.touch.about

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.aboutlibraries.ui.LibsSupportFragment
import de.nicidienase.chaosflix.touch.R

class LibsFragment : androidx.fragment.app.DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.fragment_libs, container, false)
        childFragmentManager.beginTransaction()
                .replace(R.id.layout_container, getLibsFragment())
                .commit()
        return layout
    }

    private fun getLibsFragment(): LibsSupportFragment {
        return LibsBuilder()
                .withFields(R.string::class.java.fields)
                .supportFragment()
    }
}