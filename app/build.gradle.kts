plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.dipasoftware.autodiag"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.dipasoftware.autodiag"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures{
        viewBinding = true
        buildConfig = true
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


    implementation(libs.preference)
    implementation(libs.viewpager2)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.room.runtime)


    implementation(libs.moshi)
    testImplementation(libs.mockito)
    testImplementation(libs.truth)
    implementation(libs.androidx.annotation)
    implementation(libs.fragment)
    implementation(libs.recyclerview)

    annotationProcessor(libs.room.compiler)
}