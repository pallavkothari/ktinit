package com.pk

import com.google.common.io.Files
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class FilesTests {

    @Test
    fun testLs() {
        val dir = "/tmp"
        val traverser = Files.fileTraverser()
        traverser.breadthFirst(File(dir)).forEach { file ->
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
