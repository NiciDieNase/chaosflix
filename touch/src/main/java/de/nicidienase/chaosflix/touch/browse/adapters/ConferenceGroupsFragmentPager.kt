package de.nicidienase.chaosflix.touch.browse.adapters

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.ConferenceGroup
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.touch.browse.ConferenceGroupFragment.Companion.newInstance
import java.util.ArrayList

class ConferenceGroupsFragmentPager(private val context: Context, fm: FragmentManager?) : FragmentPagerAdapter(fm!!) {

    private var conferenceGroupList: MutableList<ConferenceGroup> = ArrayList()

    override fun getItem(position: Int): Fragment {
        val conferenceGroup = conferenceGroupList[position]
        val conferenceFragment = newInstance(conferenceGroup, numColumns)
        Log.d(TAG, "Created Fragment for: " + conferenceGroup.name)
        return conferenceFragment
    }

    override fun getItemId(position: Int): Long {
        return conferenceGroupList[position].id
    }

    override fun getCount(): Int {
        return conferenceGroupList.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return conferenceGroupList[position].name
    }

    private val numColumns: Int by lazy { context.resources.getInteger(R.integer.num_columns) }

    fun setContent(conferenceGroups: MutableList<ConferenceGroup>) {
        conferenceGroupList = conferenceGroups
        notifyDataSetChanged()
    }

    companion object {
        private val TAG = ConferenceGroupsFragmentPager::class.java.simpleName
    }
}
