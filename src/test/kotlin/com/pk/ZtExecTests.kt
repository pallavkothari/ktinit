package com.pk

import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.zeroturnaround.exec.InvalidExitValueException
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.ProcessInitException
import java.util.Arrays

/**
 * tests for the `zt-exec` library
 */
class ZtExecTests {

    @Test
    fun testExitCode() {
        val exit = ProcessExecutor().command("java", "-version").execute().exitValue
        assertThat(exit, `is`(0))
    }

    @Test
    fun testMissingBinary() {
        var exit = -1
        try {
            exit = ProcessExecutor().command("lkasdjflsajf", "-version")
                .execute().exitValue
        } catch (e: ProcessInitException) {
            assertThat(exit, `is`(-1))
            return
        }

        fail()
    }

    @Test
    @Throws(Exception::class)
    fun testEnvironmentVars() {
        val env = ProcessExecutor().command("env")
            .environment("FOOBAR", "BAZ")
            .readOutput(true)
            .exitValueNormal()
            .execute()
            .outputUTF8()
        val returnedEnv = Arrays.stream(env.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            .filter { s -> s.startsWith("FOOBAR=") }
            .findFirst()
        assertTrue(returnedEnv.isPresent)
        assertThat(returnedEnv.get(), `is`("FOOBAR=BAZ"))
    }

    @Test
    @Throws(Exception::class)
    fun testNormalExitValue() {
        try {
            ProcessExecutor().command("bash", "unknown-program").exitValueNormal().execute().exitValue
            fail()
        } catch (e: InvalidExitValueException) {
            val exitValue = e.exitValue
            assertThat(exitValue, `is`(127))
        }
    }
}
