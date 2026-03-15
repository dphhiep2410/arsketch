plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.example.arsketch"
    compileSdk = 36


    defaultConfig {
        applicationId = "com.bestapp.ar.drawsketch.challenge"
        minSdk = 24
        targetSdk = 36
        versionCode = 18
        versionName = "1.1.8"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

//        manifestPlaceholders["admob_app_id"] = "ca-app-pub-3757408301807669~1046714434"
        manifestPlaceholders["admob_app_id"] = "ca-app-pub-2843552789470483~1857948787"
//        manifestPlaceholders["admob_app_id"] = "ca-app-pub-3940256099942544~3347511713"

        buildConfigField("String", "inter_test", "\"ca-app-pub-3940256099942544/1033173712\"")
        buildConfigField("String", "banner_test", "\"ca-app-pub-3940256099942544/9214589741\"")
        buildConfigField("String", "native_test", "\"/21775744923/example/native\"")
        buildConfigField("String", "open_test", "\"ca-app-pub-3940256099942544/9257395921\"")
        buildConfigField("String", "reward_test", "\"ca-app-pub-3940256099942544/5224354917\"")


        buildConfigField(
            "String", "open_resume", "\"ca-app-pub-2843552789470483/9600707432\""
        )
        buildConfigField(
            "String", "native_splash", "\"ca-app-pub-2843552789470483/3299168751\""
        )

        buildConfigField(
            "String", "inter_splash", "\"ca-app-pub-2843552789470483/1986087082\""
        )
        buildConfigField(
            "String", "native_full_splash", "\"ca-app-pub-2843552789470483/3035299085\""
        )
        buildConfigField(
            "String", "native_lang", "\"ca-app-pub-2843552789470483/6475850647\""
        )
        buildConfigField(
            "String", "native_ob1", "\"ca-app-pub-2843552789470483/5741353479\""
        )

        buildConfigField(
            "String", "native_full_ob1", "\"ca-app-pub-2843552789470483/3849687303\""
        )

        buildConfigField(
            "String", "native_ob2", "\"ca-app-pub-2843552789470483/9673005410\""
        )
        buildConfigField(
            "String", "native_full_ob2", "\"ca-app-pub-2843552789470483/1387321288\""
        )
        buildConfigField(
            "String", "native_ob3", "\"ca-app-pub-2843552789470483/8328134158\""
        )
        buildConfigField(
            "String", "inter_ob3", "\"ca-app-pub-2843552789470483/5701970812\""
        )
        buildConfigField(
            "String", "native_full_ob3", "\"ca-app-pub-4636496778732615/3679356756\""
        )
        buildConfigField(
            "String", "native_collapse", "\"ca-app-pub-2843552789470483/1826159198\""
        )
        buildConfigField(
            "String", "inter_home", "\"ca-app-pub-2843552789470483/5738793558\""
        )

        buildConfigField(
            "String", "native_permission", "\"ca-app-pub-2843552789470483/3075807473\""
        )
        buildConfigField(
            "String", "inter_back", "\"ca-app-pub-2843552789470483/9486466878\""
        )
        buildConfigField(
            "String", "native_home_id", "\"ca-app-pub-4636496778732615/4374433277\""
        )
        buildConfigField(
            "String", "native_setting", "\"ca-app-pub-2843552789470483/5170507832\""
        )
        buildConfigField(
            "String", "native_full_setting", "\"ca-app-pub-2843552789470483/1762725803\""
        )
        buildConfigField(
            "String", "native_medium_category", "\"ca-app-pub-2843552789470483/5573832516\""
        )

        buildConfigField(
            "String", "native_category_2", "\"ca-app-pub-2843552789470483/4178299876\""
        )
        buildConfigField(
            "String", "inter_draw", "\"ca-app-pub-2843552789470483/3433631201\""
        )

        buildConfigField(
            "String", "native_draw", "\"ca-app-pub-2843552789470483/5671947582\""
        )


    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }
    flavorDimensions("ads")
    productFlavors {
        create("production") {
            //flavor configurations here
            dimension = "ads"



        }
        create("staging") {
            //flavor configurations here
            dimension = "ads"


        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    bundle {
        language {
            enableSplit = false
        }
        density {
            // This property is set to true by default.
            enableSplit = true
        }
        abi {
            // This property is set to true by default.
            enableSplit = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":libads"))
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.activity:activity:1.12.3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    //lotties
    implementation("com.airbnb.android:lottie:6.1.0")
    //viewmodel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    //livedata    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    //dot
    implementation("com.tbuonomo:dotsindicator:5.1.0")
    implementation("com.github.zhpanvip:viewpagerindicator:1.2.2")
    //ssp, sdp
    implementation("com.intuit.ssp:ssp-android:1.1.1")
    implementation("com.intuit.sdp:sdp-android:1.1.1")
    //preference
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("com.google.code.gson:gson:2.13.2")

    implementation("androidx.camera:camera-core:1.5.0")
    implementation("androidx.camera:camera-camera2:1.5.0")
    implementation("androidx.camera:camera-lifecycle:1.5.0")
    implementation("androidx.camera:camera-video:1.5.0")

    implementation("androidx.camera:camera-view:1.5.0")
    implementation("androidx.camera:camera-extensions:1.5.0")

    implementation("androidx.concurrent:concurrent-futures-ktx:1.3.0")

    //photoview
//    implementation("com.github.GhayasAhmad:auto-background-remover:1.0.3")

    //glide
    implementation("com.github.bumptech.glide:glide:5.0.5")
    annotationProcessor("com.github.bumptech.glide:compiler:5.0.5")
    //download
    implementation("com.github.alexto9090:PRDownloader:1.0")
    implementation("com.github.QuadFlask:colorpicker:0.0.15")
    implementation("com.google.android.play:review:2.0.2")
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    implementation("com.google.android.play:age-signals:0.0.3")
    implementation("com.google.android.gms:play-services-ads:25.0.0")

    //crashlytic
    implementation("com.google.firebase:firebase-config:23.0.1")
    implementation("com.google.firebase:firebase-crashlytics:20.0.2")
    implementation("com.google.firebase:firebase-analytics:23.0.0")

    //draw
    implementation ("com.github.HBiSoft:HBRecorder:3.0.3")
    //
    implementation ("com.squareup.okhttp3:okhttp:5.1.0")




}