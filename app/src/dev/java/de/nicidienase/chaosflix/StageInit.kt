package de.nicidienase.chaosflix

import android.app.Application

object StageInit : ChaosflixInitializer {

    override fun init(application: Application) {}

    const val streamingApiBaseUrl = "https://streaming.test.c3voc.de"
    const val streamingApiPath = "/streams/v2.json"
}
