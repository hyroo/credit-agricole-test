package com.christo.creditagricole.data.resources

import java.io.BufferedReader
import java.io.InputStreamReader

internal actual fun loadResource(resourcePath: String): String {
    val classLoader = Thread.currentThread().contextClassLoader
        ?: ResourceLoader::class.java.classLoader
    val inputStream = classLoader?.getResourceAsStream(resourcePath)
        ?: throw IllegalArgumentException("Resource $resourcePath not found.")

    return inputStream.use { stream ->
        BufferedReader(InputStreamReader(stream)).use { reader ->
            reader.readText()
        }
    }
}

private object ResourceLoader
