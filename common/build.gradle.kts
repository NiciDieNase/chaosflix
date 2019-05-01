import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

android {
    compileSdkVersion (rootProject.ext.compileSdkVersion)
    buildToolsVersion (rootProject.ext.buildToolsVersion)

    defaultConfig {
        minSdkVersion = rootProject.ext.minSDK
        targetSdkVersion = rootProject.ext.targetSDK
        versionCode = 1
        versionName = "2.0.0"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments = mapOf("room.schemaLocation" to "$projectDir/schemas".toString())
            }
        }

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "STREAMING_API_BASE_URL", "\"https://streaming.media.ccc.de\"")
        buildConfigField("String", "STREAMING_API_OFFERS_PATH", "\"/streams/v2.json\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    lintOptions {
          abortOnError = false
    }

    buildTypes {
        debug {
            minifyEnabled = false
            useProguard = false
        }

        release {
            minifyEnabled=true
            useProguard=true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"),"proguard-rules.pro")
        }
    }

    flavorDimensions("stage", "libs")

    productFlavors {
        prod {
            dimension("stage")
            buildConfigField("String", "APPCENTER_ID", appcenterId)
        }

        dev {
            dimension("stage")
            buildConfigField("String", "APPCENTER_ID", appcenterDevId)
        }

        mock {
            dimension("stage")
            buildConfigField("String", "APPCENTER_ID", appcenterDevId)
            buildConfigField("String", "STREAMING_API_BASE_URL", "\"https://gist.githubusercontent.com\"")
            buildConfigField("String", "STREAMING_API_OFFERS_PATH", "\"/NiciDieNase/1ca017f180242f0ee683a1f592efc4ed/raw/0104592b57f4b29863fd0684a510462af276f30e/example_streams_v2.json\"")
        }

        free {
            dimension("libs")
        }
        noFree{
            dimension("libs")
        }
    }

    variantFilter { variant ->
        if (variant.buildType.name == "mockRelease") {
            setIgnore(true)
        }
    }

    dataBinding {
        enabled = true
    }

    testOptions {
        unitTests {
            includeAndroidResources = true
        }
    }
}

dependencies {
    api ( "com.android.support:appcompat-v7:${rootProject.ext.supportLibraryVersion}" )

    api ( "android.arch.lifecycle:extensions:${rootProject.ext.archCompVersion}" )
    kapt ( "android.arch.lifecycle:compiler:${rootProject.ext.archCompVersion}" )
    api ( "android.arch.lifecycle:common-java8:${rootProject.ext.archCompVersion}" )

    api ( "android.arch.persistence.room:runtime:${rootProject.ext.archCompVersion}" )
    kapt ( "android.arch.persistence.room:compiler:${rootProject.ext.archCompVersion}" )

    implementation ( "com.squareup.retrofit2:retrofit:2.3.0" )
    implementation ( "com.squareup.retrofit2:converter-gson:2.3.0" )

    api ( "com.google.android.exoplayer:exoplayer:2.9.6" )

    implementation ( "com.fasterxml.jackson.module:jackson-module-kotlin:2.9.0" )

    api ( "org.jetbrains.kotlin:kotlin-reflect:$kotlin_version" )
    api ( "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version" )

    api ( "commons-io:commons-io:2.4" )

    val appCenterSdkVersion = "1.11.4"
    noFreeImplementation ( "com.microsoft.appcenter:appcenter-analytics:${appCenterSdkVersion}" )
    noFreeImplementation ( "com.microsoft.appcenter:appcenter-crashes:${appCenterSdkVersion}" )

    debugImplementation ( "com.facebook.stetho:stetho:1.4.2" )
    debugImplementation ( "com.facebook.stetho:stetho-okhttp3:1.4.2" )
    debugImplementation ( "com.facebook.stetho:stetho-okhttp:1.4.2" )
    debugImplementation ( "com.squareup.leakcanary:leakcanary-android:1.6.3" )

    testImplementation ( "org.mockito:mockito-core:2.11.0" )
    testImplementation ( "junit:junit:4.12" )
    testImplementation ( "org.robolectric:robolectric:4.1" )
    androidTestImplementation ("com.android.support.test.espresso:espresso-core:3.0.1"){
        exclude( "com.android.support", "support-annotations")
    }

}