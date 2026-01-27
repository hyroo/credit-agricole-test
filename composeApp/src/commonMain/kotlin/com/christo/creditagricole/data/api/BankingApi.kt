package com.christo.creditagricole.data.api

import com.christo.creditagricole.data.dto.BanksResponseDto
import de.jensklingenberg.ktorfit.http.GET

interface BankingApi {

    @GET("banks.json")
    suspend fun getBanks(): BanksResponseDto
}