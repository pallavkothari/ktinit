package com.pk

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class MvnSearchTests {

    @Test
    fun guava() {
        val mvn = MavenVersion("com.google.guava", "guava")
        val latest = mvn.getLatest()
        assertThat(latest).contains("jre")
    }
}
