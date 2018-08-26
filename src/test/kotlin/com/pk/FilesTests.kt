package com.pk

import com.google.common.io.Files
import org.junit.Test
import java.io.File

class FilesTests {

    @Test
    fun testLs() {
        val dir = "/tmp"
        val traverser = Files.fileTraverser()
        for (file in traverser.breadthFirst(File(dir))) {
            println(file.absolutePath)
        }
    }
}