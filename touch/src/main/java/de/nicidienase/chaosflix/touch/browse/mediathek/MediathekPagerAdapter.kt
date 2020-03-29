package de.nicidienase.chaosflix.touch.browse.mediathek

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import de.nicidienase.chaosflix.touch.browse.download.DownloadsListFragment
import java.lang.IndexOutOfBoundsException

class MediathekPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = elements.size

    fun getItemTitle(position: Int): String {
        try {
            return elements[position].first
        } catch (ex: IndexOutOfBoundsException) {
            error("no fragment for this index")
        }
    }

    override fun createFragment(position: Int): Fragment {
        try {
            return elements[position].second()
        } catch (ex: IndexOutOfBoundsException) {
            error("no fragment for this index")
        }
    }

    companion object {

        private val elements: List<Pair<String, ()-> Fragment>> = listOf(
                "In Progress" to ::InProgressListFragment,
                "Bookmarks" to ::BookmarksListFragment,
                "Downloads" to ::DownloadsListFragment
        )
    }
}
