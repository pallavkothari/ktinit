package com.pk

import com.github.mustachejava.*
import com.github.mustachejava.reflect.GuardedBinding
import com.github.mustachejava.reflect.MissingWrapper
import com.github.mustachejava.reflect.ReflectionObjectHandler
import com.github.mustachejava.util.Wrapper
import java.io.StringReader
import java.io.StringWriter

object Mustache {
    fun merge(template: StringReader, ctx: Map<String, Any>): String {
        val mustache = mf.compile(template, "template")
        val writer = StringWriter()
        mustache.execute(writer, ctx)
        writer.flush()
        return writer.toString()
    }

    private val mf = DefaultMustacheFactory()

    // blow up if a parameter is missing
    // https://github.com/spullara/mustache.java/blob/master/compiler/src/test/java/com/github/mustachejava/FailOnMissingTest.java
    init {
        val roh: ReflectionObjectHandler = object : ReflectionObjectHandler() {
            override fun createBinding(name: String?, tc: TemplateContext, code: Code): Binding {
                return object : GuardedBinding(this, name, tc, code) {
                    @Synchronized
                    override fun getWrapper(name: String, scopes: List<Any>): Wrapper {
                        val wrapper = super.getWrapper(name, scopes)
                        if (wrapper is MissingWrapper) {
                            throw MustacheException(
                                "$name not found in $tc. current context: $scopes"
                            )
                        }
                        return wrapper
                    }
                }
            }
        }
        mf.objectHandler = roh
    }
}
