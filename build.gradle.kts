import Build_gradle.Properties.kotlin_version
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED

object Properties {
    const val kotlin_version = "1.3.60"
    const val mustache_version="0.9.6"
    const val okhttp_version="4.2.2"
    const val gson_version="2.8.6"
    const val guava_version="28.1-jre"
    const val system_rules_version="1.19.0"
    const val truth_version="1.0"
}

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.3.60"     // I would like to reference a const here but that doesn't work from the command line even tho intellij is ok with it

    // Apply the application plugin to add support for building a CLI application.
    application

    // Apply the idea plugin
    idea

    // spotless
    id("com.diffplug.gradle.spotless") version "3.26.0"
}

repositories {
    // Use jcenter for resolving dependencies.
    jcenter()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.6.0-M1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0-M1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.6.0-M1")
    testImplementation ("io.kotlintest:kotlintest:2.0.7")

    implementation("com.xenomachina:kotlin-argparser:2.0.7")
    implementation("com.squareup.moshi:moshi:1.9.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.9.1")
    implementation("org.slf4j:slf4j-api:1.8.0-beta4")
    implementation("org.slf4j:slf4j-simple:1.8.0-beta4")
    implementation("com.jayway.jsonpath:json-path:2.4.0")
    implementation("org.zeroturnaround:zt-exec:1.11")
    implementation("com.github.spullara.mustache.java:compiler:${Properties.mustache_version}")
    implementation("com.squareup.okhttp3:okhttp:${Properties.okhttp_version}")
    implementation("com.google.code.gson:gson:${Properties.gson_version}")
    implementation("com.google.guava:guava:${Properties.guava_version}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")
    testImplementation("com.github.stefanbirkner:system-rules:${Properties.system_rules_version}")
    testImplementation("com.google.truth:truth:${Properties.truth_version}")
}

group = "com.pk"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

application {
    // Define the main class for the application.
    mainClassName = "com.pk.MainKt"
}

spotless {
    kotlin {
        ktlint()
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events = setOf(PASSED, FAILED, SKIPPED)
    }
}
