plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    kotlin("android-extensions")
}

val appName = "Chaosflix"

android {
    buildToolsVersion = "27.0.3"
    compileSdkVersion = rootProject.ext.compileSdkVersion
    buildToolsVersion = rootProject.ext.buildToolsVersion
    defaultConfig {
        applicationId = "de.nicidienase.chaosflix"
        manifestPlaceholders = [label: appName]
        minSdkVersion = rootProject.ext.minSDK
        targetSdkVersion = rootProject.ext.targetSDK
        // odd for touch, even for leanback
        versionCode = rootProject.ext.leanbackVersionCode
        versionName = rootProject.ext.leanbackVersionName
//        multiDexEnabled true
    }
    signingConfigs {
        //noinspection GroovyMissingReturnStatement, GroovyAssignabilityCheck
        getByName("release") {
            if (project.hasProperty("chaosflixKeystore") && file(chaosflixKeystore).exists() && file(chaosflixKeystore).isFile()) {
                println("Release app signing is configured: will sign APK")
                storeFile = file(chaosflixKeystore)
                storePassword = chaosflixStorePassword
                keyAlias = chaosflixKeyName
                keyPassword = chaosflixKeyPassword
            } else {
                println("App signing data not found. Will not sign.")
            }
        }
    }
    buildTypes {
        getByName("debug") {
            manifestPlaceholders = [label = appName + "-dev"]
            minifyEnabled = false
            useProguard = false
        }
        getByName("release") {
            useProguard = false
            minifyEnabled = false
            shrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"),"proguard-rules.pro")
            if (project.hasProperty("chaosflixKeystore") && file(chaosflixKeystore).exists() && file(chaosflixKeystore).isFile()) {
                signingConfig = signingConfigs.release
            }
        }
    }

    flavorDimensions "stage", "libs"

    productFlavors {
        prod {
            dimension = "stage"
        }
        dev {
            dimension = "stage"
            applicationIdSuffix ".dev"
        }
        mock {
            dimension = "stage"
            applicationIdSuffix ".mock"
        }
        free {
            dimension = "libs"
        }
        noFree{
            dimension = "libs"
        }
    }

    variantFilter { variant ->
        if (variant.buildType.name == "mockRelease") {
            setIgnore(true)
        }
    }

    packagingOptions {
        exclude("META-INF/ASL2.0")
        exclude("META-INF/LICENSE")
        exclude("META-INF/license.txt")
        exclude("META-INF/NOTICE")
        exclude("META-INF/notice.txt")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    defaultConfig {
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    lintOptions {
        abortOnError = false
    }

    dataBinding {
        enabled = true
    }

    buildToolsVersion = rootProject.ext.buildToolsVersion
}

configurations {
    mockDebugCompile
    prodDebugCompile
    prodReleaseCompile
}


dependencies {
    implementation(project(":common"))
    implementation("com.android.support:cardview-v7:${rootProject.ext.supportLibraryVersion}")
    implementation("com.android.support:design:${rootProject.ext.supportLibraryVersion}")
    implementation("com.android.support:leanback-v17:${rootProject.ext.supportLibraryVersion}")
    implementation("com.android.support:preference-leanback-v17:${rootProject.ext.supportLibraryVersion}")
    implementation("com.android.support:recyclerview-v7:${rootProject.ext.supportLibraryVersion}")
    implementation("com.google.android.exoplayer:extension-leanback:2.9.6")
    implementation("com.github.bumptech.glide:glide:4.6.1")
    androidTestImplementation("com.android.support.test:rules:0.5") {
        exclude module: "support-annotations"
    }
    androidTestImplementation("com.android.support.test:runner:0.5") {
        exclude module: "support-annotations"
    }
    androidTestImplementation("com.android.support.test.uiautomator:uiautomator-v18:2.1.3")
    androidTestImplementation("org.hamcrest:hamcrest-library:1.3")
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:3.6.0")
}
