package de.nicidienase.chaosflix.touch.about


import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.FrameLayout
import de.nicidienase.chaosflix.R
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element

class AboutActivity : AppCompatActivity() {

	public override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_about)

		val toolbar = findViewById<Toolbar>(R.id.toolbar)
		toolbar.title = getString(R.string.about_chaosflix)
		setSupportActionBar(toolbar)
		supportActionBar?.setDisplayHomeAsUpEnabled(true)

		val frame = findViewById<FrameLayout>(R.id.container)

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
				.addWebsite("https://github.com/NiciDieNase/chaosflix/blob/master/LICENSE",
						getString(R.string.chaosflix_licence))
				.addWebsite("https://morr.cc/voctocat/",
						resources.getString(R.string.about_voctocat))
				.addItem(showLibs)
				.addGroup("Connect with us")
				.addGitHub("nicidienase/chaosflix", "Find the source on Github")
				.addTwitter("nicidienase","Follow the developer on Twitter")
				.addPlayStore("de.nicidienase.chaosflix", "Rate us on Google Play")
				.create()


		frame.addView(aboutView)
	}


}
