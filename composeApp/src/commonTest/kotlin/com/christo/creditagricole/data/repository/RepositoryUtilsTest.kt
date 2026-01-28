package com.christo.creditagricole.data.repository

import com.christo.creditagricole.data.exceptions.DataException
import com.christo.creditagricole.data.exceptions.NetworkException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class RepositoryUtilsTest {

    @Test
    fun `executeCall returns block result`() {
        val result = executeCall("error") { 42 }

        assertEquals(42, result)
    }

    @Test
    fun `executeCall rethrows DataException`() {
        val dataException = object : DataException("data boom") {}

        val thrown = kotlin.runCatching {
            executeCall("error") { throw dataException }
        }.exceptionOrNull()

        assertSame(dataException, thrown)
    }

    @Test
    fun `executeCall wraps unexpected exception into NetworkException`() {
        val failure = IllegalStateException("network down")

        val thrown = kotlin.runCatching {
            executeCall("Unable to fetch", block = { throw failure })
        }.exceptionOrNull()

        assertIs<NetworkException>(thrown)
        assertEquals("Unable to fetch", thrown?.message)
        assertSame(failure, thrown?.cause)
    }
}
