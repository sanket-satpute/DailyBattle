plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.symbol.processing)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.sanket_satpute_20.dailybattle"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.sanket_satpute_20.dailybattle"
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.sanket_satpute_20.dailybattle.HiltTestRunner"
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("RELEASE_KEYSTORE_PATH")
                ?: (project.findProperty("RELEASE_KEYSTORE_PATH") as? String)
                ?: System.getenv("KEYSTORE_PATH")
                ?: (project.findProperty("KEYSTORE_PATH") as? String)
            if (!keystorePath.isNullOrBlank() && file(keystorePath).exists()) {
                storeFile = file(keystorePath)
                storePassword = System.getenv("RELEASE_STORE_PASSWORD")
                    ?: (project.findProperty("RELEASE_STORE_PASSWORD") as? String)
                    ?: System.getenv("KEYSTORE_PASSWORD")
                    ?: (project.findProperty("KEYSTORE_PASSWORD") as? String)
                keyAlias = System.getenv("RELEASE_KEY_ALIAS")
                    ?: (project.findProperty("RELEASE_KEY_ALIAS") as? String)
                    ?: System.getenv("KEY_ALIAS")
                    ?: (project.findProperty("KEY_ALIAS") as? String)
                keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
                    ?: (project.findProperty("RELEASE_KEY_PASSWORD") as? String)
                    ?: System.getenv("KEY_PASSWORD")
                    ?: (project.findProperty("KEY_PASSWORD") as? String)
            }
        }
    }

    buildTypes {
        release {
            val releaseSigning = signingConfigs.getByName("release")
            signingConfig = if (releaseSigning.storeFile != null) {
                releaseSigning
            } else {
                signingConfigs.getByName("debug")
            }
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    sourceSets {
        getByName("test") {
            assets.srcDirs(files("$projectDir/schemas"))
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    kspAndroidTest(libs.androidx.room.compiler)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
