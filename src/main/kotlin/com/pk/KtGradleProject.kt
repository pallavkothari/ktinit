package com.pk

import com.google.common.base.Preconditions
import com.google.common.io.CharSource
import com.google.common.io.Files
import com.google.common.io.Resources
import org.zeroturnaround.exec.ProcessExecutor
import java.io.File
import java.io.StringReader
import kotlin.system.exitProcess

private const val EXIT_CODE_ERROR = 1

class KtGradleProject(private val params: ProjectParams) {

    fun create() {
        val proj = File(params.location, params.artifactId)
        val gradlew = setupGradleWrapper(proj)
        exec(dir = proj, cmd = listOf(gradlew, "init", "--dsl", "kotlin"), help = "please install gradle")

        process(params.overlays, proj)

        exec(dir = proj, cmd = listOf("make"))
        exec(dir = proj, cmd = listOf("make", "run"))
        setupGit(proj)
        println("😱 Generated project at $proj ")
    }

    /**
     * set up the Gradle wrapper (along with the JAR and properties file)
     * under the given project directory.
     * @return the absolute path to the (executable) gradlew script
     */
    private fun setupGradleWrapper(proj: File): String {
        // there is a gradlew file checked into source control under resources
        // at runtime we will copy it to a well-known location so that we can rely on Gradle being available
        copyResourceToDir("gradlew", proj)

        // do the same for the JAR and properties file
        val wrapperDir = File(proj.absolutePath + "/gradle/wrapper/")
        copyResourceToDir("gradle-wrapper.jar", wrapperDir)
        copyResourceToDir("gradle-wrapper.properties", wrapperDir)

        // make the gradlew script executable and return the absolute path
        val gradlew = "${proj.absolutePath}/gradlew"
        exec(dir = proj, cmd = listOf("chmod", "+x", gradlew))
        return gradlew
    }

    private fun process(overlays: List<Overlay>, proj: File) {
        overlays.forEach { overlay ->
            val template = readResource(overlay.template)
            val dest = File(proj, overlay.dest)
            Files.createParentDirs(dest)
            val merged = Mustache.merge(StringReader(template), overlay.ctx)
            CharSource.wrap(merged).copyTo(Files.asCharSink(dest, Charsets.UTF_8))
        }
    }

    private fun readResource(name: String): String {
        val url = Resources.getResource(name)
        return Resources.toString(url, Charsets.UTF_8)
    }

    private fun setupGit(proj: File) {
        // skip if running in GitHub Actions
        // GITHUB_ACTIONS will be "true" if we are
        if (System.getenv("GITHUB_ACTIONS")?.toBoolean() == true) {
            return
        }
        exec(dir = proj, cmd = listOf("git", "init"))
        exec(dir = proj, cmd = listOf("git", "add", "."))
        exec(dir = proj, cmd = listOf("git", "commit", "-m", "'init'", "--allow-empty"))
    }

    private fun exec(dir: File, cmd: List<String>, help: String = "") {
        try {
            ProcessExecutor()
                .command(cmd)
                .directory(dir)
                .exitValueNormal()
                .redirectOutput(System.out)
                .redirectErrorAlsoTo(System.out)
                .execute()
        } catch (e: Exception) {
            System.err.println(e.localizedMessage)
            println(help)
            exitProcess(EXIT_CODE_ERROR)
        }
    }
}

data class Overlay(
    val template: String,
    val dest: String,
    val ctx: Map<String, Any>
)

data class Dependency(
    val scope: String = "implementation",
    val group: String,
    val artifact: String,
    val pinnedVersion: String = ""
) {
    private val version: String by lazy {
        pinnedVersion.ifEmpty {
            MavenVersion(group, artifact).getLatest()
        }
    }

    override fun toString(): String {
        return "$scope(\"$group:$artifact:$version\")"
    }
}

// not terribly exciting: just looks up the resource identified by fileName
// and copies it to the given destination folder
fun copyResourceToDir(fileName: String, destFolder: File) {
    destFolder.mkdirs()
    Preconditions.checkArgument(destFolder.exists() && destFolder.isDirectory, "could not mkdirs for $destFolder")
    val fileToMove = Resources.getResource(fileName)
    File(destFolder, fileName).outputStream().use {
        Resources.copy(fileToMove, it)
    }
}
