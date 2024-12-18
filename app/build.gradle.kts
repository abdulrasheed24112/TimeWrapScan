plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("com.google.gms.google-services")
}

android {
    namespace = "com.development.nest.time.wrap"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.development.nest.time.wrap"
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {

    implementation ("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation ("androidx.navigation:navigation-ui-ktx:2.7.7")


    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-messaging:23.4.1")
    // implementation ("com.github.thelumiereguy:NeumorphismView-Android:1.5")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("com.github.fornewid:neumorphism:0.3.2")


    implementation ("com.github.bumptech.glide:glide:4.14.2")
    //kapt ("com.github.bumptech.glide:compiler:4.14.2")

    implementation ("com.airbnb.android:lottie:5.2.0")

    implementation ("me.zhanghai.android.materialratingbar:library:1.4.0")

    implementation ("com.guolindev.permissionx:permissionx:1.7.1")

//    def nav_version = '2.5.3'
    implementation ("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation ("androidx.navigation:navigation-ui-ktx:2.7.7")
    platform("com.google.firebase:firebase-bom:32.1.0")

    implementation ("com.github.fornewid:neumorphism:0.3.2")




}