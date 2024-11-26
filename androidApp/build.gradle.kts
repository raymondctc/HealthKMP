@file:Suppress("UNUSED_VARIABLE")

import java.util.Properties

val versionMajor = 1
val versionMinor = 0
val versionPatch = 1
val isSnapshotVersion = false

val versionNameGradle: String
    get() {
        val versionString = "${versionMajor}.${versionMinor}.${versionPatch}"

        if (isSnapshotVersion) {
            return "${versionString}-SNAPSHOT"
        }
        return versionString
    }
val versionCodeGradle: Int
    get() {
        return versionMajor * 10000 + versionMinor * 100 + versionPatch
    }

plugins {
    kotlin("multiplatform")
    id("com.android.application")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

kotlin {
    androidTarget()
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation("com.vitoksmile.health-kmp:koin:0.0.3")
                implementation(project(":sample"))

                implementation("androidx.activity:activity-compose:1.9.2")
//                implementation("androidx.credentials:credentials:1.3.0")
//                implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
//                implementation("com.google.android.libraries.identity.googleid.googleid:1.1.1")
            }
        }
    }
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "com.ninegag.moves.app"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        applicationId = "com.ninegag.moves.android.app"
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
        versionCode = versionCodeGradle
        versionName = versionNameGradle
    }

    signingConfigs {
        create("release") {
            val properties = Properties().apply {
                load(File("$rootDir/signing.properties").reader())
            }
            storeFile = file("$rootDir/${properties.getProperty("storeFilePath")}")
            storePassword = properties.getProperty("storePassword")
            keyPassword = properties.getProperty("keyPassword")
            keyAlias = properties.getProperty("keyAlias")
        }
    }

    dependencies {
        coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }

        getByName("debug") {
            applicationIdSuffix = ".dev"
            isDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
        isCoreLibraryDesugaringEnabled = true
    }
    kotlin {
        jvmToolchain(18)
    }
}
