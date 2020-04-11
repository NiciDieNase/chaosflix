package de.nicidienase.chaosflix.touch.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import de.nicidienase.chaosflix.touch.R
import de.nicidienase.chaosflix.touch.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil
                .setContentView<ActivitySettingsBinding>(this, R.layout.activity_settings)
        setSupportActionBar(binding.toolbarInc.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.preferences)
    }
}
