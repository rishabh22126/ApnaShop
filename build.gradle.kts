// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// ✅ Add this block at the bottom
buildscript {
    dependencies {
        // Google services Gradle plugin (for Firebase)
        classpath("com.google.gms:google-services:4.4.2")
    }
}
