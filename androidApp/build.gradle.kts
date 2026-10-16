import java.util.Properties
import java.util.Base64
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

val cloudSettings = Properties().apply {
    val file = rootProject.file("admin/.env.local")
    if (file.exists()) file.inputStream().use { load(it) }
}
fun publicSetting(name: String): String = providers.environmentVariable(name).orNull
    ?: providers.gradleProperty(name).orNull ?: cloudSettings.getProperty(name, "").trim().trim('"', '\'')
fun quoted(value: String) = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
val publicCloudKey = publicSetting("VITE_SUPABASE_ANON_KEY")
require(!publicCloudKey.startsWith("sb_secret_")) { "Only a public Supabase key may be embedded" }
if (publicCloudKey.startsWith("eyJ")) {
    val payload = String(Base64.getUrlDecoder().decode(publicCloudKey.split('.')[1]))
    require(!payload.contains("service_role")) { "Never embed a service role key" }
}
val isPreviewApp = providers.gradleProperty("previewApp").orNull == "true"
val isLiveQaApp = providers.gradleProperty("liveQaApp").orNull == "true"
require(!(isPreviewApp && isLiveQaApp)) { "Choose either offline preview or live QA" }
android {
    namespace = "com.homeapp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = when { isPreviewApp -> "com.homeapp.preview"; isLiveQaApp -> "com.homeapp.liveqa"; else -> "com.homeapp" }
        manifestPlaceholders["appLabel"] = when { isPreviewApp -> "0Brocker Preview"; isLiveQaApp -> "0Brocker Live QA"; else -> "0Brocker" }
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        buildConfigField("String", "SUPABASE_URL", quoted(publicSetting("VITE_SUPABASE_URL")))
        buildConfigField("String", "SUPABASE_KEY", quoted(publicCloudKey))
        buildConfigField("boolean", "IS_PREVIEW", isPreviewApp.toString())
        versionCode = 5
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
        buildConfig = true
    }
}

// Name each variant's APK and export every produced APK into <project root>/apk
val projectApkDir = rootProject.layout.projectDirectory.dir("apk")
androidComponents {
    onVariants(selector().all()) { variant ->
        val upperVariant = variant.name.replaceFirstChar(Char::uppercase)
        val apkName = when { isPreviewApp -> "0Brocker-home-preview.apk"; isLiveQaApp -> "0Brocker-live-qa.apk"; else -> "0Brocker-v1.0.0-test-${variant.buildType}.apk" }
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
