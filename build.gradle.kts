import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED

object Properties {
    const val kotlin_version = "1.3.72"
    const val mustache_version = "0.9.6"
    const val okhttp_version = "4.7.2"
    const val gson_version = "2.8.6"
    const val guava_version = "29.0-jre"
    const val system_rules_version = "1.19.0"
    const val truth_version = "1.0.1"
    const val jupiter_version = "5.6.2"
    const val argparser_version = "2.0.7"
    const val moshi_version = "1.9.2"
    const val slf4j_version = "1.7.30"
    const val json_path_version = "2.4.0"
    const val zt_exec_version = "1.11"
}

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.3.72"     // I would like to reference a const here but that doesn't work from the command line even tho Intellij is OK with it

    // Apply the application plugin to add support for building a CLI application.
    application

    // Apply the idea plugin
    idea

    // spotless
    id("com.diffplug.gradle.spotless") version "4.0.1"

    // this plugin helps us publish to maven repositories (like github packages)
    `maven-publish`

    jacoco

    id("com.github.ben-manes.versions") version "0.28.0"
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

    testImplementation("org.junit.jupiter:junit-jupiter-engine:${Properties.jupiter_version}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${Properties.jupiter_version}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${Properties.jupiter_version}")

    implementation("com.xenomachina:kotlin-argparser:${Properties.argparser_version}")
    implementation("com.squareup.moshi:moshi:${Properties.moshi_version}")
    implementation("com.squareup.moshi:moshi-kotlin:${Properties.moshi_version}")
    implementation("org.slf4j:slf4j-api:${Properties.slf4j_version}")
    implementation("org.slf4j:slf4j-simple:${Properties.slf4j_version}")
    implementation("com.jayway.jsonpath:json-path:${Properties.json_path_version}")
    implementation("org.zeroturnaround:zt-exec:${Properties.zt_exec_version}")
    implementation("com.github.spullara.mustache.java:compiler:${Properties.mustache_version}")
    implementation("com.squareup.okhttp3:okhttp:${Properties.okhttp_version}")
    implementation("com.google.code.gson:gson:${Properties.gson_version}")
    implementation("com.google.guava:guava:${Properties.guava_version}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Properties.kotlin_version}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${Properties.kotlin_version}")
    testImplementation("com.github.stefanbirkner:system-rules:${Properties.system_rules_version}")
    testImplementation("com.google.truth:truth:${Properties.truth_version}")
}

group = "com.pk"
version = "0.0.2"
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

// https://help.github.com/en/github/managing-packages-with-github-packages/configuring-gradle-for-use-with-github-packages
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/pallavkothari/ktinit")

            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
        // easier to debug with ./gradlew publishKtinitPublicationToInternalRepository
        maven {
            name = "internal"
            url = uri("$buildDir/repos/internal")
        }
    }
    publications {
        register("ktinit", MavenPublication::class) {
            from(components["java"])
        }
    }
}

// build sources jar too
java {
    withSourcesJar()
}
