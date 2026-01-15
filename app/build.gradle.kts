plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") // version is inherited from the root
}

android {
    namespace = "com.example.home"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.home"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    val nav_version = "2.7.7"
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
	implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
	implementation("androidx.legacy:legacy-support-v4:1.0.0")
	implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
	implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
}
