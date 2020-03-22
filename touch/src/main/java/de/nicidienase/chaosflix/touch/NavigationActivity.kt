package de.nicidienase.chaosflix.touch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import de.nicidienase.chaosflix.touch.databinding.ActivityNavigationBinding

class NavigationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navController = findNavController(R.id.nav_host)
        binding.bottomNavigation.setupWithNavController(navController)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId){
            android.R.id.home -> findNavController(R.id.nav_host).navigateUp()
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private val TAG = NavigationActivity::class.java.simpleName

        fun launch(context: Context) {
            context.startActivity(Intent(context, NavigationActivity::class.java))
        }
    }
}
