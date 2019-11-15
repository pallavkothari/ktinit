package com.pk

import com.google.common.io.CharSource
import com.google.common.io.Files
import com.google.common.io.Resources
import org.zeroturnaround.exec.ProcessExecutor
import java.io.File
import java.io.StringReader
import kotlin.system.exitProcess

class KtGradleProject(private val params: ProjectParams) {

    fun create() {
        val proj = File(params.location, params.artifactId)
        proj.mkdirs()

        val userDir = System.getProperty("user.dir")
        val gradlew = "$userDir/gradlew"
        exec(dir = proj, cmd = listOf(gradlew, "init"), help = "please install gradle")

        process(params.overlays, proj)

        exec(dir = proj, cmd = listOf("make"))
        exec(dir = proj, cmd = listOf("make", "run"))
        setupGit(proj)

        println("\uD83D\uDE31 Generated project at $proj ")
    }

    private fun process(overlays: List<Overlay>, proj: File) {
        for (overlay in overlays) {
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
            exitProcess(1)
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
        if (pinnedVersion.isNotEmpty()) {
            pinnedVersion
        } else {
            MavenVersion(group, artifact).getLatest()
        }
    }

    override fun toString(): String {
        return "$scope \"$group:$artifact:$version\""
    }
}