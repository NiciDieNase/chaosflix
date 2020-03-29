package de.nicidienase.chaosflix.touch.browse.mediathek

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import de.nicidienase.chaosflix.touch.databinding.FragmentMediathekBinding

class MediathekFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentMediathekBinding.inflate(inflater)

        val mediathekPagerAdapter = MediathekPagerAdapter(this)
        binding.viewpager.adapter = mediathekPagerAdapter

        TabLayoutMediator(binding.tabs, binding.viewpager) { tab, position ->
            tab.text = mediathekPagerAdapter.getItemTitle(position)
        }.attach()

        return binding.root
    }
}
