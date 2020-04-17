package de.nicidienase.chaosflix.touch.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import de.nicidienase.chaosflix.common.ChaosflixPreferenceManager
import de.nicidienase.chaosflix.touch.BuildConfig
import de.nicidienase.chaosflix.touch.R
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.about_chaosflix)

        val chaosflixPreferenceManager = ChaosflixPreferenceManager(PreferenceManager.getDefaultSharedPreferences(requireContext()))

        val showLibs = Element().apply {
            title = resources.getString(R.string.showLibs)
            onClickListener =
                View.OnClickListener { LibsFragment().show(childFragmentManager, null) }
        }
        val privacyPolicy = Element().apply {
            title = "Privacy Policy"
            onClickListener = View.OnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("Privacy Policy")
                    .setMessage(R.string.privacy_policy)
                    .create().show()
            }
        }

        val version = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        val versionElement = Element()
        var clickCounter = 0
        versionElement.title = "Version $version"
        versionElement.setOnClickListener {
            when (clickCounter++) {
                10 -> {
                    chaosflixPreferenceManager.debugEnabled = true
                    showToast(R.string.debug_enabled)
                }
                9 -> showToast(R.string.one_more_time)
                8 -> showToast(R.string.debug_soon)
            }
        }

        return AboutPage(requireContext())
            .setImage(R.drawable.icon_primary_background)
            .setDescription(resources.getString(R.string.about_description))
            .addItem(versionElement)
            .addWebsite(
                    getString(R.string.about_licence_url),
                    getString(R.string.chaosflix_licence)
            )
            .addWebsite(
                    getString(R.string.about_voctocat_url),
                resources.getString(R.string.about_voctocat)
            )
            .addItem(showLibs)
            .addItem(privacyPolicy)
            .addGroup("Connect with us")
            .addGitHub("nicidienase/chaosflix", getString(R.string.about_github))
            .addWebsite(getString(R.string.about_beta_url), getString(R.string.about_beta))
//            .addTwitter("nicidienase", getString(R.string.about_twitter))
                .addTwitter("chaosflix_app")
            .addPlayStore("de.nicidienase.chaosflix", getString(R.string.about_playstore))
            .create()
    }

    private fun showToast(@StringRes message: Int) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
