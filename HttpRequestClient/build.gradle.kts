import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "com.murattarslan.httprequestclient"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.murattarslan"
            artifactId = "httprequestclient"
            version = "1.0"
            artifact("${buildDir}/outputs/aar/httpRequestClient-release.aar")
        }
    }

    repositories {
        maven {
            name = "githubPackages"
            url = uri("https://maven.pkg.github.com/murattarslan/httpRequestClient")
            credentials {
                val localProperties = Properties()
                val localPropertiesFile = rootProject.file("local.properties")
                if (localPropertiesFile.exists()) {
                    localProperties.load(localPropertiesFile.inputStream())
                }
                username = localProperties.getProperty("GITHUB_USER")
                println("username:" + username)
                password = localProperties.getProperty("GITHUB_TOKEN")
                println("password" + password)
            }
        }

    }
}

dependencies {

    implementation(libs.kotlin.reflect)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}