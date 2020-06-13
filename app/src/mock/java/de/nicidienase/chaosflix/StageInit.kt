package de.nicidienase.chaosflix

import android.app.Application

object StageInit : ChaosflixInitializer {

    override fun init(application: Application) {}

    const val streamingApiBaseUrl = "https://gist.githubusercontent.com"
    const val streamingApiPath = "/NiciDieNase/1ca017f180242f0ee683a1f592efc4ed/raw/0104592b57f4b29863fd0684a510462af276f30e/example_streams_v2.json"
}
