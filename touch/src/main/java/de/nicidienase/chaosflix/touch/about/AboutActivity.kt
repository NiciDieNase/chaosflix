package de.nicidienase.chaosflix.touch.about

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.touch.databinding.ActivityAboutBinding
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element

class AboutActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityAboutBinding>(
            this, R.layout.activity_about
        )

        binding.toolbarInc.toolbar.title = getString(R.string.about_chaosflix)
        setSupportActionBar(binding.toolbarInc.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val showLibs = Element().apply {
            title = resources.getString(R.string.showLibs)
            onClickListener =
                View.OnClickListener { LibsFragment().show(supportFragmentManager, null) }
        }
        val privacyPolicy = Element().apply {
            title = "Privacy Policy"
            onClickListener = View.OnClickListener {
                AlertDialog.Builder(this@AboutActivity)
                    .setTitle("Privacy Policy")
                    .setMessage(R.string.privacy_policy)
                    .create().show()
            }

        }


        val pInfo = packageManager.getPackageInfo(packageName, 0)
        val version = pInfo.versionName
        val aboutView = AboutPage(this)
            .setImage(R.drawable.icon_notext)
            .setDescription(resources.getString(R.string.description))
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
            .addTwitter("nicidienase", getString(R.string.about_twitter))
            .addPlayStore("de.nicidienase.chaosflix", getString(R.string.about_playstore))
            .create()

        binding.container.addView(aboutView)
    }
}
