package de.nicidienase.chaosflix.touch

import android.arch.lifecycle.Observer
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import de.nicidienase.chaosflix.common.mediadata.sync.Downloader
import de.nicidienase.chaosflix.common.viewmodel.SplashViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.touch.browse.BrowseActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startConferencesActivity()

//        val viewModel = ViewModelProviders.of(this, ViewModelFactory(this))
//            .get(SplashViewModel::class.java)
//
//        viewModel.conferencesAvailable?.observe(this, Observer {
//            if (it == true){
//                Log.i(TAG, "There are some conferences available, starting activity ...")
//                startConferencesActivity()
//            } else {
//                Log.i(TAG, "No conferences available, loading conferences ...")
//                viewModel.updateConferences().observe(this, Observer {
//                    Log.i(TAG, "Stage: ${it?.state}")
//                    if (it?.state == Downloader.DownloaderState.DONE) {
//                        startConferencesActivity()
//                    }
//                })
//            }
//        }) ?: startConferencesActivity()
    }

    private fun startConferencesActivity() {
        Log.i(TAG, "Starting Conferences Activity")
        startActivity(Intent(this, BrowseActivity::class.java))
        finish()
    }

    companion object {
        private val TAG = SplashActivity::class.java.simpleName
    }
}