package com.christo.creditagricole.data.network

import com.christo.creditagricole.data.api.BankingApi
import com.christo.creditagricole.data.dto.BanksResponseDto
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.takeFrom

/**
 * Local replacement for Ktorfit generated code. The interface retains the
 * Ktorfit annotations but we provide the runtime implementation manually to
 * keep the data layer testable without the compiler plugin.
 */
fun Ktorfit.createBankingApi(): BankingApi {
    val ktorfit = this
    return object : BankingApi {

        private val client = ktorfit.httpClient
        private val fullBanksUrl = "${ktorfit.baseUrl.trimEnd('/')}/banks.json"

        override suspend fun getBanks(): BanksResponseDto = client
            .get {
                url {
                    takeFrom(fullBanksUrl)
                }
            }
            .body()
    }
}
