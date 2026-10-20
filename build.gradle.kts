import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
id("com.android.application")
id("org.jetbrains.kotlin.android")
}

android {
namespace = "com.kociarcade"
compileSdk = 35

defaultConfig {
    applicationId = "com.kociarcade"
    minSdk = 23
    targetSdk = 35
    versionCode = 1
    versionName = "1.0"
}

compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlinOptions {
    jvmTarget = "17"
}

}

tasks.withType<JavaCompile>().configureEach {
sourceCompatibility = "17"
targetCompatibility = "17"
}

tasks.withType<KotlinCompile>().configureEach {
kotlinOptions {
jvmTarget = "17"
}
}
