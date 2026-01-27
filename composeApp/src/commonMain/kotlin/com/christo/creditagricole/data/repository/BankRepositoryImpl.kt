package com.christo.creditagricole.data.repository

import com.christo.creditagricole.data.api.BankingApi
import com.christo.creditagricole.data.mapper.toDomain
import com.christo.creditagricole.domain.model.Bank
import com.christo.creditagricole.domain.repository.BankRepository

class BankRepositoryImpl(
    private val api: BankingApi
) : BankRepository {

    override suspend fun getBanks(): List<Bank> =
        executeCall("Impossible de recuperer les banques") {
            api.getBanks().banks.map { it.toDomain() }

        }

    override suspend fun getMockBanks(): List<Bank> {
        TODO("Not yet implemented")
    }
}