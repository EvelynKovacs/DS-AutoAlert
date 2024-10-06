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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation("androidx.test:core:1.5.0")
    implementation("androidx.test.ext:junit:1.2.1")
    implementation("androidx.test.espresso:espresso-core:3.6.1")
    //implementation("org.mockito:mockito-core:4.6.1") // Verifica la última versión
    implementation("org.mockito:mockito-android:5.0.0")
    implementation("org.robolectric:robolectric:4.7.3")


}