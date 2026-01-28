package com.christo.creditagricole.data.repository

import com.christo.creditagricole.data.api.BankingApi
import com.christo.creditagricole.data.mapper.toDomain
import com.christo.creditagricole.domain.model.Bank
import com.christo.creditagricole.domain.repository.BankRepository
import creditagricole.composeapp.generated.resources.Res
import creditagricole.composeapp.generated.resources.data_fetch_banks_error
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.getString

class BankRepositoryImpl(
    private val api: BankingApi
) : BankRepository {

    override suspend fun getBanks(): List<Bank> =
        executeCall(getString(Res.string.data_fetch_banks_error)) {
            api.getBanks().map { it.toDomain() }

        }

    override suspend fun getMockBanks(): List<Bank> {
        TODO("Not yet implemented")
    }
}
