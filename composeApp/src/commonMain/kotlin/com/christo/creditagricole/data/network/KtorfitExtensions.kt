package com.christo.creditagricole.data.network

import com.christo.creditagricole.data.api.BankingApi
import com.christo.creditagricole.data.dto.AccountDto
import com.christo.creditagricole.data.dto.AccountsResponseDto
import com.christo.creditagricole.data.dto.BankDto
import com.christo.creditagricole.data.dto.OperationDto
import com.christo.creditagricole.data.dto.OperationsResponseDto
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.appendPathSegments
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
        private val baseUrl = ktorfit.baseUrl.trimEnd('/')

        override suspend fun getBanks(): List<BankDto> {
            val response = client
                .get {
                    url {
                        takeFrom(baseUrl)
                        appendPathSegments("banks.json")
                    }
                }
            val payload: List<BankDto> = response.body<List<BankDto>>()
            return payload
        }

        override suspend fun getAccountsForBank(bankId: String): AccountsResponseDto = client
            .get {
                url {
                    takeFrom(baseUrl)
                    appendPathSegments("banks", bankId, "accounts")
                }
            }
            .body()

        override suspend fun getAccount(accountId: String): AccountDto = client
            .get {
                url {
                    takeFrom(baseUrl)
                    appendPathSegments("accounts", accountId)
                }
            }
            .body()

        override suspend fun getOperationsForAccount(accountId: String): OperationsResponseDto =
            client
                .get {
                    url {
                        takeFrom(baseUrl)
                        appendPathSegments("accounts", accountId, "operations")
                    }
                }
                .body()

        override suspend fun getOperation(operationId: String): OperationDto = client
            .get {
                url {
                    takeFrom(baseUrl)
                    appendPathSegments("operations", operationId)
                }
            }
            .body()
    }
}
