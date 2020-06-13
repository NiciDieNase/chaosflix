package de.nicidienase.chaosflix

import android.content.Context
import java.io.File

class ChaosflixStageConfiguration(context: Context) : StageConfiguration {
    override val versionName: String = BuildConfig.VERSION_NAME
    override val versionCode: Int = BuildConfig.VERSION_CODE
    override val recordingUrl = context.resources.getString(R.string.recording_url) ?: "https://api.media.ccc.de"
    override val eventInfoUrl = context.resources.getString(R.string.event_info_url) ?: "https://c3voc.de"
    override val cacheDir: File? = context.cacheDir
    override val streamingApiBaseUrl = StageInit.streamingApiBaseUrl
    override val streamingApiPath = StageInit.streamingApiPath
    override val appcenterId: String? = BuildConfig.APPCENTER_ID
    override val externalFilesDir: File? = android.os.Environment.getExternalStorageDirectory()
}
