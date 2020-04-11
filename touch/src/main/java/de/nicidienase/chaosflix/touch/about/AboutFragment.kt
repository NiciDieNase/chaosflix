package de.nicidienase.chaosflix.touch.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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

        val pInfo = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
        val version = pInfo.versionName
        val aboutView = AboutPage(requireContext())
            .setImage(R.drawable.icon_primary_background)
            .setDescription(resources.getString(R.string.about_description))
            .addItem(Element().setTitle("Version $version"))
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

        return aboutView
    }
}
