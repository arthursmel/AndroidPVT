// Top-level build file where you can add configuration options common to all sub-projects/modules.

val kotlinVersion: String by extra

buildscript {

    val kotlinVersion by System.getProperties()

    repositories {
        mavenCentral()
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files

        classpath("com.vanniktech:gradle-maven-publish-plugin:0.13.0")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.4.10.2")
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
        jcenter()
    }
}

tasks {
    val clean by registering(Delete::class) {
        delete(rootProject.buildDir)
    }
}
