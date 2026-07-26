import java.util.Properties

// 1. قراءة ملف local.properties بشكل آمن قبل تهيئة إعدادات الأندرويد
val localProperties = Properties()
val localPropertiesFile = rootDir.resolve("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.sharpspeak"

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.sharpspeak"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 2. استدعاء المفتاح من الملف وتمريره إلى BuildConfig بأمان
        val apiKey = localProperties.getProperty("GEMINI_API_KEY") ?: ""
        buildConfigField("String", "GEMINI_API_KEY", "\"$apiKey\"")
    }

    // 3. تفعيل توليد كلاس BuildConfig
    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// 4. تجميع كل المكتبات في بلوك واحد احترافي
dependencies {
    // مكتبة Google AI SDK لـ Gemini
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // لسهولة التعامل مع التزامن والمهام الخلفية في أندرويد
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.8.0")

    // مكتبة للطلبات الشبكية
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // مكتبات أندرويد الأساسية
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    // مكتبات الاختبار
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}