import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "com.homeapp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.homeapp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 4
        versionName = "1.0.0-test.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
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
    buildFeatures {
        compose = true
    }
}

// Name each variant's APK and export every produced APK into <project root>/apk
val projectApkDir = rootProject.layout.projectDirectory.dir("apk")
androidComponents {
    onVariants(selector().all()) { variant ->
        val upperVariant = variant.name.replaceFirstChar(Char::uppercase)
        val apkName = "0Brocker-v1.0.0-test-${variant.buildType}.apk"
        val apkOutputs = layout.buildDirectory.dir("outputs/apk/${variant.name}")
        tasks.register<Copy>("exportApk$upperVariant") {
            dependsOn("assemble$upperVariant")
            from(apkOutputs)
            into(projectApkDir)
            include("*.apk")
            rename { _ -> apkName }
        }
        tasks.configureEach {
            if (name == "assemble$upperVariant") {
                finalizedBy("exportApk$upperVariant")
            }
        }
    }
}