// Top-level build file where you can add configuration options common to all sub-projects/modules.

import java.net.URI

buildscript {
    repositories {
        mavenLocal()
        google()
        jcenter()
        maven("https://plugins.gradle.org/m2/")
//        maven { url = URI("https://plugins.gradle.org/m2/") }
    }
    dependencies {
        classpath("com.android.tools.build","gradle","3.4.0")
        classpath("org.jetbrains.kotlin","kotlin-gradle-plugin", "1.3.21")
        classpath("org.jlleitschuh.gradle","ktlint-gradle","7.4.0")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        mavenLocal()
        google()
        jcenter()
        maven { url = URI("https://oss.sonatype.org/content/repositories/snapshots/") }
        maven { url = URI("https://jitpack.io") }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

apply(plugin = "org.jlleitschuh.gradle.ktlint-idea")

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}

