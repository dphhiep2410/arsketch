// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven {
            url = uri("https://jitpack.io")
            url = uri("https://artifact.bytedance.com/repository/pangle/")
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
        }
        maven {
            url =
                uri("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea")
        }

    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.7")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.48.1")
        classpath("com.android.tools.build:gradle:8.13.0")
    }
}

plugins {
    id("com.android.application") version "8.13.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven {
            url = uri("https://artifact.bytedance.com/repository/pangle")
        }
        maven { url = uri("https://jitpack.io") }
        maven {
            url = uri("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea")
        }
    }
}