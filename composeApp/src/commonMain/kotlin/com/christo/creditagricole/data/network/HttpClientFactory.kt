package com.christo.creditagricole.data.network

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object NetworkClientFactory {
    fun createKtorfit(baseUrl: String, json: Json = defaultJson()): Ktorfit =
        Ktorfit.Builder()
            .baseUrl(baseUrl)
            .httpClient(httpClient(json))
            .build()
}

fun defaultJson(): Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
}

expect fun httpClient(json: Json): HttpClient

fun HttpClientConfig<*>.configureCommon(json: Json) {
    install(ContentNegotiation) {
        json(json)
    }
    install(Logging)
}
