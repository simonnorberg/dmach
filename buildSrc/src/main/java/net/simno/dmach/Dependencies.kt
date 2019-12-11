@file:Suppress("unused")

package net.simno.dmach

object Versions {
    const val compileSdk = 29
    const val targetSdk = 29
    const val minSdk = 21
    const val buildTools = "29.0.2"
    const val ndk = "20.1.5948944"
    const val java = "1.8"
    const val ktlint = "0.33.0"
}

object Libs {
    const val androidGradlePlugin = "com.android.tools.build:gradle:3.6.0-beta05"
    const val playPublisherPlugin = "com.github.triplet.gradle:play-publisher:2.6.1"
    const val junit = "junit:junit:4.13-rc-2"
    const val robolectric = "org.robolectric:robolectric:4.3.1"
    const val mockito = "org.mockito:mockito-core:3.2.0"
    const val rxbinding = "com.jakewharton.rxbinding3:rxbinding:3.1.0"
    const val rxrelay = "com.jakewharton.rxrelay2:rxrelay:2.1.1"
    const val rxjava = "io.reactivex.rxjava2:rxjava:2.2.15"
    const val rxandroid = "io.reactivex.rxjava2:rxandroid:2.1.1"
    const val kortholt = "net.simno.kortholt:kortholt:1.3.0"
    const val dmachExternals = "net.simno.dmach:dmach-externals:1.3.0"

    object Google {
        const val ossLicensesPlugin = "com.google.android.gms:oss-licenses-plugin:0.10.0"
        const val ossLicensesLibrary = "com.google.android.gms:play-services-oss-licenses:17.0.0"
        const val truth = "com.google.truth:truth:1.0"
    }

    object Kotlin {
        private const val version = "1.3.61"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$version"
        const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
        const val serializationPlugin = "org.jetbrains.kotlin:kotlin-serialization:$version"
        const val serializationRuntime = "org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.14.0"
    }

    object AndroidX {
        const val activity = "androidx.activity:activity-ktx:1.1.0-rc03"
        const val appcompat = "androidx.appcompat:appcompat:1.1.0"
        const val constraintlayout = "androidx.constraintlayout:constraintlayout:2.0.0-beta3"
        const val core = "androidx.core:core-ktx:1.2.0-rc01"
        const val dynamicanimation = "androidx.dynamicanimation:dynamicanimation:1.1.0-alpha03"
        const val recyclerview = "androidx.recyclerview:recyclerview:1.1.0"

        object Lifecycle {
            private const val version = "2.2.0-rc03"
            const val extensions = "androidx.lifecycle:lifecycle-extensions:$version"
            const val viewmodel = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
        }

        object Paging {
            private const val version = "2.1.0"
            const val common = "androidx.paging:paging-common-ktx:$version"
            const val runtime = "androidx.paging:paging-runtime-ktx:$version"
            const val rxjava2 = "androidx.paging:paging-rxjava2-ktx:$version"
        }

        object Room {
            private const val version = "2.2.2"
            const val runtime = "androidx.room:room-runtime:$version"
            const val compiler = "androidx.room:room-compiler:$version"
            const val rxjava2 = "androidx.room:room-rxjava2:$version"
        }

        object Test {
            const val core = "androidx.test:core:1.3.0-alpha03"
            const val junit = "androidx.test.ext:junit:1.1.2-alpha03"
            const val runner = "androidx.test:runner:1.3.0-alpha03"
        }
    }
}
