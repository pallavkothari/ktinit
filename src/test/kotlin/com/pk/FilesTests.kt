package com.pk

import com.google.common.io.Files
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.Test

class FilesTests {

    @Test
    fun testLs() {
        val dir = "/tmp"
        val traverser = Files.fileTraverser()
        for (file in traverser.breadthFirst(File(dir))) {
            println(file.absolutePath)
        }
    }

    @Test
    fun copyResourceToFile() {
        val tmp = Files.createTempDir()
        copyResourceToDir("gradlew", tmp)
        val gradlew = File(tmp, "gradlew")
        assertTrue(gradlew.exists())
        assertTrue(gradlew.isFile)
    }
}
