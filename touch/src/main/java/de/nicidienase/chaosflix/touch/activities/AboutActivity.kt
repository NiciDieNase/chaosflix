package de.nicidienase.chaosflix.touch.activities


import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.aboutlibraries.ui.LibsFragment
import com.mikepenz.aboutlibraries.ui.LibsSupportFragment
import de.nicidienase.chaosflix.R

class AboutActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "About Chaosflix"
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

//        val aboutView = AboutPage(this).setImage(R.drawable.icon_notext_144x144)
//                .setDescription(resources.getString(R.string.about_description))
//                .addItem(Element().setTitle("Version ${BuildConfig.VERSION_NAME}"))
//                .addPlayStore("de.nicidienase.chaosflix")
//                .addGitHub("nicidienase/chaosflix")
//                .create()

//        val frame = findViewById<FrameLayout>(R.id.container)
//        frame.addView(aboutView)

        val aboutView: LibsSupportFragment = LibsBuilder()
                .supportFragment()
        supportFragmentManager.beginTransaction()
                .replace(R.id.container,aboutView)
                .commit()
    }
}
