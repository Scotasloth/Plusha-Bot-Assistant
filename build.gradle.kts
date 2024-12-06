// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false

}

buildscript {
    dependencies {
        classpath (libs.kotlinGradlePlugin)
    }
    repositories {
        google()
    }
}

allprojects {
    repositories {
        google()  // For Android dependencies
        mavenCentral()  // For libraries hosted on Maven Central
    }
}


