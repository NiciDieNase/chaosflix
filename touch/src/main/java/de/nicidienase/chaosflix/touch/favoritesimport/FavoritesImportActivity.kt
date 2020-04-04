package de.nicidienase.chaosflix.touch.favoritesimport

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import de.nicidienase.chaosflix.touch.R

class FavoritesImportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites_import)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.import_activity_label)
    }

    companion object {
        private val TAG = FavoritesImportActivity::class.java.simpleName
    }
}
