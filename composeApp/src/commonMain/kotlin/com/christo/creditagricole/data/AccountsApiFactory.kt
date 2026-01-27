package com.christo.creditagricole.data

import com.christo.creditagricole.data.api.BankingApi
import com.christo.creditagricole.data.dto.BanksResponseDto
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

internal fun Ktorfit.createAccountsApi(json: Json): BankingApi =
    ManualBankingApi(
        client = httpClient,
        baseUrl = baseUrl,
        json = json
    )

private class ManualBankingApi(
    private val client: HttpClient,
    private val baseUrl: String,
    private val json: Json
) : BankingApi {

    override suspend fun getBanks(): BanksResponseDto {
        val url = baseUrl.trimEnd('/') + "/banks.json"
        val response = client.get(url)
        val payload = response.bodyAsText()
        return json.decodeFromString(payload)
    }
}
