package de.nicidienase.chaosflix.touch

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.nicidienase.chaosflix.touch.browse.BrowseActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivity(Intent(this, BrowseActivity::class.java))
        finish()
    }
}