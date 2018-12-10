package de.nicidienase.chaosflix.touch.about


import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.databinding.ActivityAboutBinding
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element

class AboutActivity : AppCompatActivity() {

	public override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val binding = DataBindingUtil.setContentView<ActivityAboutBinding>(
				this, R.layout.activity_about)

		binding.toolbarInc.toolbar.title = getString(R.string.about_chaosflix)
		setSupportActionBar(binding.toolbarInc.toolbar)
		supportActionBar?.setDisplayHomeAsUpEnabled(true)

		val showLibs = Element()
		showLibs.title = resources.getString(R.string.showLibs)
		showLibs.onClickListener = object : View.OnClickListener {

			override fun onClick(p0: View?) {
				LibsFragment().show(supportFragmentManager, null)
			}
		}

		val pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		val version = pInfo.versionName;
		val aboutView = AboutPage(this)
				.setImage(R.drawable.icon_notext_144x144)
				.setDescription(resources.getString(R.string.description))
				.addItem(Element().setTitle("Version ${version}"))
				.addWebsite(getString(R.string.about_licence_url),
						getString(R.string.chaosflix_licence))
				.addWebsite(getString(R.string.about_voctocat_url),
						resources.getString(R.string.about_voctocat))
				.addItem(showLibs)
				.addGroup("Connect with us")
				.addGitHub("nicidienase/chaosflix", getString(R.string.about_github))
				.addWebsite(getString(R.string.about_beta_url), getString(R.string.about_beta))
				.addTwitter("nicidienase", getString(R.string.about_twitter))
				.addPlayStore("de.nicidienase.chaosflix", getString(R.string.about_playstore))
				.create()

		binding.container.addView(aboutView)
	}


}
