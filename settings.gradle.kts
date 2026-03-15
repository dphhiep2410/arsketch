pluginManagement {
    repositories {
        google()
        mavenCentral()
        jcenter()
        gradlePluginPortal()
        maven {
            url = uri("https://artifact.bytedance.com/repository/pangle")

        }
        maven { url = uri("https://jitpack.io") }
        maven {
            url =
                uri("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea")
        }

        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
        }

    }
}

rootProject.name = "ArSketch"
include(":app")
include(":libads")

