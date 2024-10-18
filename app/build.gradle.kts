plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.autoalert"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.autoalert"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }


}
dependencies {

    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.media3.common)
    val room_version = "2.6.1"
    val lifecycle_version = "2.8.5"

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.jjoe64:graphview:4.2.2") // Verificar compatibilidad con AndroidX
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("de.hdodenhof:circleimageview:3.1.0")


    implementation ("com.github.anastr:speedviewlib:1.6.1")

    // ROOM Librerías
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
}
