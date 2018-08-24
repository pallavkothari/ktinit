package com.pk

import com.github.mustachejava.DefaultMustacheFactory
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.StringReader
import java.io.StringWriter

class KtInitTests {

	@Test
	fun contextLoads() {
		val template = StringReader("Hello {{name}}!")
        val ctx = mapOf("name" to "world")
        val mf = DefaultMustacheFactory()
        val mustache = mf.compile(template, "template")
        val writer = StringWriter()
        mustache.execute(writer, ctx)
        writer.flush()
        assertThat(writer.toString()).isEqualTo("Hello world!")
	}

}
