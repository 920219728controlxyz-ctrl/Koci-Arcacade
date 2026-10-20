import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
id("com.android.application") version "8.7.3" apply false
id("org.jetbrains.kotlin.android") version "2.0.21" apply false
}

subprojects {
tasks.withType<JavaCompile>().configureEach {
sourceCompatibility = "17"
targetCompatibility = "17"
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
    }
}

}
