@file:Suppress("UNUSED_VARIABLE")

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("plugin.serialization")
}

kotlin {
    androidTarget()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }

    // https://youtrack.jetbrains.com/issue/KT-42254
    // Fix iOS building to real device's issue of "Duplicate symbols"
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        binaries.withType<org.jetbrains.kotlin.gradle.plugin.mpp.Framework> {
            isStatic = false
        }
    }

    cocoapods {
        name = "HealthKMPSample"
        version = "0.0.3"
        summary = "Wrapper for HealthKit on iOS and Google Fit and Health Connect on Android."
        homepage = "https://github.com/vitoksmile/HealthKMP"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "HealthKMPSample"
            isStatic = true
        }
        pod("FirebaseCore") {
            linkOnly = true
            version = "11.2.0"
        }
        pod("FirebaseAuth") {
            linkOnly = true
            version = "11.2.0"
        }
        pod("FirebaseFirestore") {
            linkOnly = true
            version = "11.2.0"
        }
        pod("FirebaseAnalytics") {
            linkOnly = true
            version = "11.2.0"
        }
        pod("FirebaseRemoteConfig") {
            linkOnly = true
            version = "11.2.0"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.vitoksmile.health-kmp:core:0.0.3")
                api(project.dependencies.platform("io.github.tweener:kmp-bom:2.0.4")) // Mandatory
                api("io.github.tweener:kmp-firebase")
                api("io.insert-koin:koin-core:3.5.6")
                api("io.github.aakira:napier:2.7.1")

                // Coil image lib
                implementation("io.coil-kt.coil3:coil-compose:3.0.3")
                implementation("io.coil-kt.coil3:coil-network-ktor3:3.0.3")

                // Kotlinx Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
            }
        }

        val androidMain by getting {
            dependencies {
                api("io.insert-koin:koin-android:3.5.6")
                implementation("io.ktor:ktor-client-android:3.0.1")
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

            dependencies {
                // Workaround for https://youtrack.jetbrains.com/issue/KT-41821
                implementation("org.jetbrains.kotlinx:atomicfu:0.21.0")
                implementation("io.ktor:ktor-client-darwin:3.0.1")
            }
        }
    }
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "com.ninegag.move.kmp"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
    }
    dependencies {
        coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlin {
        jvmToolchain(17)
    }
}
dependencies {
    implementation("androidx.navigation:navigation-runtime-ktx:2.8.4")
}
