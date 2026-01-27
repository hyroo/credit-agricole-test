package com.christo.creditagricole.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import kotlinx.serialization.json.Json

actual fun httpClient(json: Json): HttpClient =
    HttpClient(Darwin) {
        configureCommon(json)
    }
