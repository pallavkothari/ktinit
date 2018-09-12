package com.pk

import com.google.common.io.Files
import com.pk.Option.*
import java.io.File

fun main(args: Array<String>) {
    val inputs = mutableMapOf<Option, Any>()
    for (option in listOf(GROUP_ID, ARTIFACT_ID)) {
        print("enter ${option.name}:")
        val input = readLine()!!
        inputs[option] = input
    }
    val projectParams = ProjectParams(
        groupId = inputs[GROUP_ID] as String,
        artifactId = inputs[ARTIFACT_ID] as String,
        overlays = buildOverlaysForSimpleProject(inputs)
    )

    KtGradleProject(projectParams).create()
}

data class ProjectParams(
    val groupId: String,
    val artifactId: String,
    val location: File = Files.createTempDir(),
    val overlays: List<Overlay>
)

// TODO Option to pass these in
fun dependencies(): List<Dependency> = listOf(
    Dependency("implementation", "org.slf4j", "slf4j-api"),
    Dependency("implementation", "org.slf4j", "slf4j-simple"),
    Dependency("implementation", "com.squareup.okhttp3", "okhttp"),
    Dependency("implementation", "com.google.code.gson", "gson"),
    Dependency("implementation", "com.google.guava", "guava"),
    Dependency("testImplementation", "io.kotlintest", "kotlintest"),
    Dependency("testImplementation", "com.github.stefanbirkner", "system-rules"),
    Dependency("testImplementation", "com.google.truth", "truth")
)

fun buildOverlaysForSimpleProject(inputs: MutableMap<Option, Any>, deps: List<Dependency> = dependencies()): List<Overlay> {
    // build ctx to pass to mustache
    inputs[MAIN_CLASS] = "${inputs[GROUP_ID]}.${inputs[ARTIFACT_ID]}.MainKt"
    inputs[DEPS] = deps
    val ctx = inputs.mapKeys { it.key.templateName }
    val group = inputs[GROUP_ID]!!.toString().replace(".", "/")
    val pkg = "$group/${inputs[ARTIFACT_ID]}"

    return listOf(
        Overlay("build.gradle.mustache", "build.gradle", ctx),
        Overlay("gradle.properties.mustache", "gradle.properties", ctx),
        Overlay("Makefile.mustache", "Makefile", ctx),
        Overlay("README.md.mustache", "README.md", ctx),
        Overlay("gitignore.mustache", ".gitignore", ctx),
        Overlay("Main.mustache", "src/main/kotlin/$pkg/Main.kt", ctx),
        Overlay("SillyTest.kt.mustache", "src/test/kotlin/$pkg/SillyTest.kt", ctx)
    )
}

enum class Option(val templateName: String) {
    GROUP_ID("groupId"),
    ARTIFACT_ID("artifactId"),
    MAIN_CLASS("mainClass"),
    DEPS("deps")
}

