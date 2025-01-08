plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.trackticum"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.trackticum"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures{
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.android.volley:volley:1.2.1")
    implementation ("com.makeramen:roundedimageview:2.3.0")
    implementation ("com.google.android.flexbox:flexbox:3.0.0")
    implementation ("com.github.dhaval2404:imagepicker:2.1")
    implementation ("com.squareup.picasso:picasso:2.8")
    implementation ("com.github.androidmads:QRGenerator:1.0.5")
    implementation ("com.airbnb.android:lottie:6.6.2")
    implementation ("com.github.yuriy-budiyev:code-scanner:2.3.0")
    implementation ("com.karumi:dexter:6.2.3")
    implementation ("com.itextpdf:itextpdf:5.5.13")
}