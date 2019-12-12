package com.pk

import com.google.common.base.CaseFormat
import com.google.common.io.Files
import com.pk.Option.ARTIFACT_ID
import com.pk.Option.DEPS
import com.pk.Option.GROUP_ID
import com.pk.Option.MAIN_CLASS
import com.pk.Option.NO_ARG_PARSING
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.DefaultHelpFormatter
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import java.io.File

class MyArgs(parser: ArgParser) {
    val currentDir by parser.flagging(
        "-c", "--current-dir",
        help = "create project in current directory"
    )

    val groupId by parser.storing(
        "-g", "--group-id",
        help = "the group ID for this project"
    ).default("com.example")

    val artifactId by parser.storing(
        "-a", "--artifact-id",
        help = "the artifact ID for this project"
    ) {
        CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, this) // convert hyphens to camel case
    }.default("ktfoo")

    val dependencies by parser.adding(
        "-d", "--dep",
        help = "provide additional dependencies in the format <groupId>:<artifactId>[:version]"
    ) {
        val parts = this.split(":")
        when (parts.size) {
            2 -> Dependency(group = parts[0], artifact = parts[1])
            3 -> Dependency(group = parts[0], artifact = parts[1], pinnedVersion = parts[2])
            else -> throw Exception(this)
        }
    }.default<List<Dependency>>(listOf())

    val noArgs by parser.flagging(
        "-n", "--no-arg-parsing",
        help = "don't add command line arg parsing capabilities"
    )
}

fun main(args: Array<String>) = mainBody {
    ArgParser(
        args,
        helpFormatter = DefaultHelpFormatter(
            epilogue = "Sample usage: " +
                "ktinit -c --group-id com.pk --artifact-id kotlindemo -d com.google.guava:guava [--no-arg-parsing]"
        )
    ).parseInto(::MyArgs).run {
        if (currentDir) println("Creating project in current directory.")

        val inputs = mutableMapOf<Option, Any>(
            GROUP_ID to groupId,
            ARTIFACT_ID to artifactId,
            NO_ARG_PARSING to noArgs
        )

        val defaultDependencies = dependencies().toMutableList()

        if (noArgs) {
            defaultDependencies.removeIf { dep -> dep.artifact == "kotlin-argparser" }
        }

        val projectParams = ProjectParams(
            groupId = groupId,
            artifactId = artifactId,
            overlays = buildOverlaysForSimpleProject(inputs, deps = dependencies.union(defaultDependencies)),
            location = if (currentDir) File(System.getProperty("user.dir")) else Files.createTempDir()
        )

        KtGradleProject(projectParams).create()

        if (!currentDir) println("\nYou may use the project above or run `ktinit --help` to see more options.")

        if (noArgs) println("Disabling command-line parsing.")
    }
}

data class ProjectParams(
    val groupId: String,
    val artifactId: String,
    val location: File = Files.createTempDir(),
    val overlays: List<Overlay>
)

fun dependencies(): List<Dependency> = listOf(
    Dependency("implementation", "org.slf4j", "slf4j-api"),
    Dependency("implementation", "org.slf4j", "slf4j-simple"),
    Dependency("implementation", "com.squareup.okhttp3", "okhttp"),
    Dependency("implementation", "com.google.code.gson", "gson"),
    Dependency("implementation", "com.google.guava", "guava"),
    Dependency("testImplementation", "com.github.stefanbirkner", "system-rules"),
    Dependency("testImplementation", "com.google.truth", "truth"),
    Dependency("testRuntimeOnly", "org.junit.jupiter", "junit-jupiter-engine"),
    Dependency("testImplementation", "org.junit.jupiter", "junit-jupiter-api"),
    Dependency("testImplementation", "org.junit.jupiter", "junit-jupiter-params"),
    Dependency("testRuntimeOnly", "org.junit.platform", "junit-platform-console"),
    Dependency("implementation", "com.xenomachina", "kotlin-argparser")
)

fun buildOverlaysForSimpleProject(
    inputs: MutableMap<Option, Any>,
    deps: Iterable<Dependency> = dependencies()
): List<Overlay> {
    // build ctx to pass to mustache
    inputs[MAIN_CLASS] = "${inputs[GROUP_ID]}.${inputs[ARTIFACT_ID]}.MainKt"
    inputs[DEPS] = deps
    val ctx = inputs.mapKeys { it.key.templateName }
    val group = inputs[GROUP_ID]!!.toString().replace(".", "/")
    val pkg = "$group/${inputs[ARTIFACT_ID]}"

    return listOf(
        Overlay("build.gradle.kts.mustache", "build.gradle.kts", ctx),
        Overlay("gradle.properties.mustache", "gradle.properties", ctx),
        Overlay("Makefile.mustache", "Makefile", ctx),
        Overlay("README.md.mustache", "README.md", ctx),
        Overlay("gitignore.mustache", ".gitignore", ctx),
        Overlay("Main.mustache", "src/main/kotlin/$pkg/Main.kt", ctx),
        Overlay("SillyTest.kt.mustache", "src/test/kotlin/$pkg/SillyTest.kt", ctx)
    )
}

// keeping these around for mustache context keys
enum class Option(val templateName: String) {
    GROUP_ID("groupId"),
    ARTIFACT_ID("artifactId"),
    MAIN_CLASS("mainClass"),
    DEPS("deps"),
    NO_ARG_PARSING("noArgs");
}
