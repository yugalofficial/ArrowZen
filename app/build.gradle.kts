import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
}

val appPackageName: String = (project.findProperty("ARROWZEN_PACKAGE_NAME") as String?) ?: "com.yugalify.arrowzen"
val appVersionCode: Int = ((project.findProperty("ARROWZEN_VERSION_CODE") as String?) ?: "1").toInt()
val appVersionName: String = (project.findProperty("ARROWZEN_VERSION_NAME") as String?) ?: "1.0.0"

// AdMob is opt-in (Section 33 / docs/ADMOB.md). Default OFF: the app compiles
// and ships with zero third-party ad-SDK dependency until this is explicitly
// turned on in gradle.properties.
val enableAdmob: Boolean = (project.findProperty("ARROWZEN_ENABLE_ADMOB") as String?)?.toBoolean() ?: false
val adsEnabledAtRuntime: Boolean = (project.findProperty("ARROWZEN_ADS_ENABLED") as String?)?.toBoolean() ?: enableAdmob
val admobAppId: String = (project.findProperty("ARROWZEN_ADMOB_APP_ID") as String?) ?: ""
val admobRewardedUnitId: String = (project.findProperty("ARROWZEN_ADMOB_REWARDED_UNIT_ID") as String?) ?: ""
val admobInterstitialUnitId: String = (project.findProperty("ARROWZEN_ADMOB_INTERSTITIAL_UNIT_ID") as String?) ?: ""
val admobBannerUnitId: String = (project.findProperty("ARROWZEN_ADMOB_BANNER_UNIT_ID") as String?) ?: ""

// Optional local signing config. Never commit keystore.properties or the keystore file itself.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
val hasSigningConfig = keystorePropertiesFile.exists()
if (hasSigningConfig) {
    keystoreProperties.load(keystorePropertiesFile.inputStream())
}

android {
    namespace = appPackageName
    compileSdk = 36

    defaultConfig {
        applicationId = appPackageName
        minSdk = 26
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        buildConfigField("boolean", "ADS_ENABLED", adsEnabledAtRuntime.toString())
        buildConfigField("String", "ADMOB_APP_ID", "\"$admobAppId\"")
        buildConfigField("String", "ADMOB_REWARDED_UNIT_ID", "\"$admobRewardedUnitId\"")
        buildConfigField("String", "ADMOB_INTERSTITIAL_UNIT_ID", "\"$admobInterstitialUnitId\"")
        buildConfigField("String", "ADMOB_BANNER_UNIT_ID", "\"$admobBannerUnitId\"")

        // Inert unless the play-services-ads dependency is actually present
        // (ARROWZEN_ENABLE_ADMOB=true) -- see docs/ADMOB.md.
        manifestPlaceholders["admobAppId"] = admobAppId.ifBlank { "ca-app-pub-3940256099942544~3347511713" }
    }

    if (hasSigningConfig) {
        signingConfigs {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (hasSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // Exactly one of these compiles: the dependency-free NoOp factory by
    // default, or the real Google Mobile Ads-backed factory when
    // ARROWZEN_ENABLE_ADMOB=true. See docs/ADMOB.md.
    sourceSets {
        getByName("main") {
            java.srcDir(if (enableAdmob) "src/admob/java" else "src/noAdmob/java")
        }
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.03")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-process:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.8.1")

    // Local storage
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // JSON parsing for local puzzle data
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")

    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Only pulled in when ARROWZEN_ENABLE_ADMOB=true (see docs/ADMOB.md).
    // Default builds have zero dependency on this SDK.
    if (enableAdmob) {
        implementation("com.google.android.gms:play-services-ads:23.6.0")
    }
}
