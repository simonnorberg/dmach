@file:Suppress("unused")

package net.simno.dmach

object Versions {
    const val compileSdk = 30
    const val targetSdk = 30
    const val minSdk = 26
    const val ndk = "22.0.7026061"
    const val java = "1.8"
    const val ktlint = "0.40.0"
}

object Libs {
    const val androidGradlePlugin = "com.android.tools.build:gradle:4.1.2"
    const val playPublisherPlugin = "com.github.triplet.gradle:play-publisher:3.2.0"
    const val junit = "junit:junit:4.13.1"
    const val robolectric = "org.robolectric:robolectric:4.5"
    const val mockito = "org.mockito:mockito-core:3.7.7"
    const val insetter = "dev.chrisbanes.insetter:insetter-widgets:0.4.0"
    const val kortholt = "net.simno.kortholt:kortholt:1.7.0"
    const val dmachExternals = "net.simno.dmach:dmach-externals:1.7.0"

    object Google {
        const val ossLicensesPlugin = "com.google.android.gms:oss-licenses-plugin:0.10.2"
        const val ossLicensesLibrary = "com.google.android.gms:play-services-oss-licenses:17.0.0"
        const val truth = "com.google.truth:truth:1.1.2"

        object Hilt {
            private const val version = "2.31.2-alpha"
            const val android = "com.google.dagger:hilt-android:$version"
            const val compiler = "com.google.dagger:hilt-android-compiler:$version"
            const val plugin = "com.google.dagger:hilt-android-gradle-plugin:$version"
        }
    }

    object Kotlin {
        private const val version = "1.4.21"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$version"
        const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
        const val serializationPlugin = "org.jetbrains.kotlin:kotlin-serialization:$version"
        const val serializationRuntime = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1"
    }

    object Coroutines {
        private const val version = "1.4.2"
        const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
        const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
    }

    object AndroidX {
        const val activity = "androidx.activity:activity-ktx:1.2.0-rc01"
        const val appcompat = "androidx.appcompat:appcompat:1.3.0-beta01"
        const val constraintlayout = "androidx.constraintlayout:constraintlayout:2.0.4"
        const val core = "androidx.core:core-ktx:1.5.0-beta01"
        const val dynamicanimation = "androidx.dynamicanimation:dynamicanimation:1.1.0-alpha03"
        const val fragment = "androidx.fragment:fragment-ktx:1.3.0-rc02"
        const val recyclerview = "androidx.recyclerview:recyclerview:1.2.0-beta01"

        object Hilt {
            private const val version = "1.0.0-alpha03"
            const val viewmodel = "androidx.hilt:hilt-lifecycle-viewmodel:$version"
            const val compiler = "androidx.hilt:hilt-compiler:$version"
        }

        object Lifecycle {
            private const val version = "2.3.0-rc01"
            const val livedata = "androidx.lifecycle:lifecycle-livedata-ktx:$version"
            const val runtime = "androidx.lifecycle:lifecycle-runtime-ktx:$version"
            const val viewmodelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
            const val viewmodelState = "androidx.lifecycle:lifecycle-viewmodel-savedstate:$version"
        }

        object Paging {
            private const val version = "3.0.0-alpha13"
            const val common = "androidx.paging:paging-common:$version"
            const val runtime = "androidx.paging:paging-runtime:$version"
        }

        object Room {
            private const val version = "2.3.0-beta01"
            const val common = "androidx.room:room-common:$version"
            const val runtime = "androidx.room:room-runtime:$version"
            const val ktx = "androidx.room:room-ktx:$version"
            const val compiler = "androidx.room:room-compiler:$version"
            const val testing = "androidx.room:room-testing:$version"
        }

        object Test {
            const val core = "androidx.test:core:1.3.0"
            const val junit = "androidx.test.ext:junit:1.1.2"
            const val runner = "androidx.test:runner:1.3.0"
            const val espresso = "androidx.test.espresso:espresso-core:3.3.0"
        }
    }

    object LeakCanary {
        private const val version = "2.6"
        const val leakcanary = "com.squareup.leakcanary:leakcanary-android:$version"
        const val plumber = "com.squareup.leakcanary:plumber-android:$version"
    }
}
