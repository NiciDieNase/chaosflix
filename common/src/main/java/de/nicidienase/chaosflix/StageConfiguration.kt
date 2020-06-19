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
    val deviceBrand: String
    val deviceModel: String
    val osReleaseVersion: String

    fun buildUserAgent(): String {
        val versionName = this.versionName
        val device = "${this.deviceBrand} ${this.deviceModel}"
        val osVersion = "Android/${this.osReleaseVersion}"
        return "chaosflix/$versionName $osVersion ($device)"
    }
}
