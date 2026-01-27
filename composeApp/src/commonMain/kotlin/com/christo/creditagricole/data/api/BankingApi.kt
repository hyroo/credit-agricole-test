package com.christo.creditagricole.data.api

import com.christo.creditagricole.data.dto.AccountDto
import com.christo.creditagricole.data.dto.AccountsResponseDto
import com.christo.creditagricole.data.dto.BankDto
import com.christo.creditagricole.data.dto.OperationDto
import com.christo.creditagricole.data.dto.OperationsResponseDto
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path

interface BankingApi {

    @GET("banks.json")
    suspend fun getBanks(): List<BankDto>

    @GET("banks/{bankId}/accounts")
    suspend fun getAccountsForBank(
        @Path("bankId") bankId: String
    ): AccountsResponseDto

    @GET("accounts/{accountId}")
    suspend fun getAccount(
        @Path("accountId") accountId: String
    ): AccountDto

    @GET("accounts/{accountId}/operations")
    suspend fun getOperationsForAccount(
        @Path("accountId") accountId: String
    ): OperationsResponseDto

    @GET("operations/{operationId}")
    suspend fun getOperation(
        @Path("operationId") operationId: String
    ): OperationDto
}
