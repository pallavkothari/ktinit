package com.pk

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Test


class JsonTests {

    @Test
    fun moshi() {
        val dependency = Dependency(scope = "implementation", group = "com.google.guava", artifact = "guava")
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val adapter = moshi.adapter(Dependency::class.java)

        val json = adapter.toJson(dependency)
        assertThat(json).isEqualTo("""{"scope":"implementation","group":"com.google.guava","artifact":"guava","pinnedVersion":""}""")

        val gav = """{"group":"com.google.guava","artifact":"guava"}"""
        val fromJson = adapter.fromJson(gav)
        println("parsed $fromJson")
        assertThat(fromJson.toString()).contains("""implementation "com.google.guava:guava:""")
    }

    @Test
    fun listOfDeps() {
        val deps = listOf(
            Dependency(group = "com.google.guava", artifact = "guava"),
            Dependency(group = "com.google.truth", artifact = "truth")
        )
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val type = Types.newParameterizedType(List::class.java, Dependency::class.java)
        val adapter: JsonAdapter<List<Dependency>> = moshi.adapter(type)
        val json = adapter.toJson(deps)
        println("json = $json")


        val input = """
            [
                {"group":"com.google.guava","artifact":"guava"},
                {"group":"com.google.truth","artifact":"truth"}
            ]
        """.trimIndent()
        val output = adapter.fromJson(input)
        println("output = $output")
    }
}
