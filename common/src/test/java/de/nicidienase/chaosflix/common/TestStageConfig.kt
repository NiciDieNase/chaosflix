package de.nicidienase.chaosflix.common

import de.nicidienase.chaosflix.StageConfiguration
import java.io.File

object TestStageConfig : StageConfiguration {
    override val versionName: String = "0.1"
    override val versionCode: Int = 1
    override val recordingUrl: String = "https://api.media.ccc.de"
    override val eventInfoUrl: String = "https://c3voc.de"
    override val cacheDir: File? = null
    override val externalFilesDir: File? = null
    override val streamingApiBaseUrl: String = "https://streaming.media.ccc.de"
    override val streamingApiPath: String = "/streams/v2.json"
    override val appcenterId: String? = null
}
