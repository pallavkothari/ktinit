package com.pk

import com.google.common.io.Resources
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File
import java.io.StringReader

class KtInitTests {

    @Test
    fun genProject() {
        val groupId = "com.pk"
        val artifactId = "test"
        val inputs = mutableMapOf<Option, Any>(
            Option.GROUP_ID to groupId,
            Option.ARTIFACT_ID to artifactId
        )
        val params = ProjectParams(
            groupId = groupId,
            artifactId = artifactId,
            overlays = buildOverlaysForSimpleProject(
                inputs,
                listOf(
                    Dependency("compile", "com.google.guava", "guava", pinnedVersion = "26.0-jre"),
                    Dependency("testCompile", "com.google.truth", "truth"),
                    Dependency("testImplementation", "org.junit.jupiter", "junit-jupiter-engine"),
                    Dependency("testImplementation", "org.junit.jupiter", "junit-jupiter-api"),
                    Dependency("testImplementation", "org.junit.jupiter", "junit-jupiter-params")
                )
            )
        )

        KtGradleProject(params).create()
    }

    @Test
    fun stache() {
        val template = StringReader("Hello {{name}}!")
        val ctx = mapOf("name" to "world")
        val merged = Mustache.merge(template, ctx)
        assertThat(merged).isEqualTo("Hello world!")
    }

    @Test
    fun resource() {
        val name = "hello.mustache"
        val template = readResource(name)
        assertThat(template).isEqualTo("Hello {{name}}!")
    }

    private fun readResource(name: String): String {
        val url = Resources.getResource(name)
        return Resources.toString(url, Charsets.UTF_8)
    }

    @Test
    fun buildGradleFile() {
        val template = readResource("build.gradle.mustache")
        val ctx = mapOf(
            "groupId" to "com.pk",
            "artifactId" to "testing",
            "mainClass" to "com.pk.MainKt",
            "deps" to listOf(Dependency("compile", "com.google.guava", "guava"))
        )
        val merged = Mustache.merge(StringReader(template), ctx)
        println("merged = $merged")
    }

    @Test
    fun testDeps() {
        val groupId = "com.google.guava"
        val artifactId = "guava"
        val version = "foo"
        val dep = Dependency("compile", groupId, artifactId, version)
        println(dep)
        val expected = "compile \"com.google.guava:guava:foo\""
        assertThat(dep.toString()).isEqualTo(expected)

        val template = """
            {{#deps}}
                {{{.}}}
            {{/deps}}
        """.trimIndent()
        val ctx = mapOf("deps" to listOf(dep))
        val merged = Mustache.merge(StringReader(template), ctx as Map<String, Any>)
        println("merged = $merged")
        assertThat(merged.trim()).isEqualTo(expected)
    }

}
