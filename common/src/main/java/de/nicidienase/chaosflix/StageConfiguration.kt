package de.nicidienase.chaosflix

import java.io.File

interface StageConfiguration {
    val versionName: String
    val versionCode: Int
    val recordingUrl: String
    val eventInfoUrl: String
    val cacheDir: File?
    val externalFilesDir: File?
    val streamingApiBaseUrl: String
    val streamingApiPath: String
    val appcenterId: String?
}
