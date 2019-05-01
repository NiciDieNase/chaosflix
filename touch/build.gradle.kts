import org.gradle.internal.impldep.com.amazonaws.PredefinedClientConfigurations.defaultConfig
import de.nicidienase.chaosflix.build.Versions

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    kotlin("android.extensions")
}

val appName: String = "Chaosflix"

android {

    compileSdkVersion(rootProject.ext.compileSdkVersion)
    buildToolsVersion (rootProject.ext.buildToolsVersion )

    defaultConfig {
        applicationId = "de.nicidienase.chaosflix"
        minSdkVersion(22)
        targetSdkVersion(28)
        manifestPlaceholders = [label to appName]
        // odd for touch, even for leanback
        versionCode = rootProject.ext.touchVersionCode
        versionName = rootProject.ext.touchVersionName
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        //noinspection GroovyMissingReturnStatement, GroovyAssignabilityCheck
        release {
            if (project.hasProperty("chaosflixKeystore") && file(chaosflixKeystore).exists() && file(chaosflixKeystore).isFile()) {
                println "Release app signing is configured: will sign APK"
                storeFile = file(chaosflixKeystore)
                storePassword = chaosflixStorePassword
                keyAlias = chaosflixKeyName
                keyPassword = chaosflixKeyPassword
            } else {
                println "App signing data not found. Will not sign."
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".dev"
            manifestPlaceholders = [label: appName + "-dev"]
            minifyEnabled = false
            useProguard = false
        }
        release {
            useProguard = true
            minifyEnabled = true
            shrinkResources = true
            proguardFiles = getDefaultProguardFile("proguard-android.txt"),"proguard-rules.pro"
            if (project.hasProperty("chaosflixKeystore") && file(chaosflixKeystore).exists() && file(chaosflixKeystore).isFile()) {
                signingConfig = signingConfigs.release
            }
        }
    }

    flavorDimensions "stage", "libs"

    productFlavors {
        create("prod") {
            require(this is ExtensionAware)
            dimension = "stage"
        }
        create("dev") {
            require(this is ExtensionAware)
            dimension = "stage"
            applicationIdSuffix = ".dev"
        }
        create("mock") {
            require(this is ExtensionAware)
            dimension = "stage"
            applicationIdSuffix = ".mock"
        }
        create("free") {
            require(this is ExtensionAware)
            dimension = "libs"
        }
        create("noFree"){
            require(this is ExtensionAware)
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

    lintOptions {
        abortOnError = false
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

configurations {
    mockDebugCompile
    prodDebugCompile
    prodReleaseCompile
}

dependencies {
    implementation(project(":common"))
    implementation("com.mikepenz:aboutlibraries:6.1.1@aar") {
        transitive = true
    }
    implementation ("com.github.medyo:android-about-page:1.2.2")
    implementation ("com.android.support:support-v4:${rootProject.ext.supportLibraryVersion}")
    implementation ("com.android.support:recyclerview-v7:${rootProject.ext.supportLibraryVersion}")
    implementation ("com.android.support:cardview-v7:${rootProject.ext.supportLibraryVersion}")
    implementation ("com.android.support.constraint:constraint-layout:${rootProject.ext.constraintLayoutVersion}")
    implementation ("com.android.support:design:${rootProject.ext.supportLibraryVersion}")
    implementation ("com.android.support:preference-v14:${rootProject.ext.supportLibraryVersion}")
    implementation ("com.android.support:mediarouter-v7:${rootProject.ext.supportLibraryVersion}")
    noFreeImplementation ("com.google.android.gms:play-services-cast-framework:16.2.0")
    noFreeImplementation("pl.droidsonroids:casty:1.0.8") {
        exclude group: "com.android.support", module: "appcompat-v7"
    }
    implementation ("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.0")

    implementation ("com.github.bumptech.glide:glide:4.6.1")

    implementation ("net.opacapp:multiline-collapsingtoolbar:27.1.1")
    implementation ("net.rdrei.android.dirchooser:library:3.2@aar")
    //    implementation ("com.gu:option:1.3")
    implementation ("com.github.guardian:Option:-SNAPSHOT")
    testImplementation ("junit:junit:4.12")
    testImplementation ("org.mockito:mockito-core:2.11.0")
    testImplementation ("org.robolectric:robolectric:4.1")
    androidTestImplementation("com.android.support.test:rules:0.5") {
        exclude module: "support-annotations"
    }
    androidTestImplementation("com.android.support.test:runner:0.5") {
        exclude module: "support-annotations"
    }
    androidTestImplementation ("com.android.support.test.uiautomator:uiautomator-v18:2.1.3")
    androidTestImplementation ("org.hamcrest:hamcrest-library:1.3")
    androidTestImplementation ("com.android.support.test.espresso:espresso-core:2.2.2", {
        exclude group: "com.android.support", module: "support-annotations"
    })
}