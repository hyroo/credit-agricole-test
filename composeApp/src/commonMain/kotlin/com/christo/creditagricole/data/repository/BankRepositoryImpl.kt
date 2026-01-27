package com.christo.creditagricole.data.repository

import com.christo.creditagricole.data.api.BankingApi
import com.christo.creditagricole.data.exceptions.DataException
import com.christo.creditagricole.data.exceptions.NetworkException
import com.christo.creditagricole.data.local.BanksFixtures
import com.christo.creditagricole.data.mapper.toDomain
import com.christo.creditagricole.domain.model.Bank
import com.christo.creditagricole.domain.repository.BankRepository

class BankRepositoryImpl(
    private val api: BankingApi
) : BankRepository {

    override suspend fun getBanks(): List<Bank> {
        val remoteBanks = runCatching {
            executeCall("Impossible de recuperer les banques") {
                api.getBanks().banks.map { it.toDomain() }
            }
        }.getOrElse { return loadMockBanks() }

        return if (remoteBanks.isEmpty()) {
            loadMockBanks()
        } else {
            remoteBanks
        }
    }

    override suspend fun getMockBanks(): List<Bank> = loadMockBanks()
}

private fun loadMockBanks(): List<Bank> = parseBanksResource()
    .banks
    .map { it.toDomain() }

private fun parseBanksResource() = BanksFixtures.response

private inline fun <T> executeCall(
    errorMessage: String,
    block: () -> T
): T = try {
    block()
} catch (exception: DataException) {
    throw exception
} catch (throwable: Throwable) {
    throw NetworkException(errorMessage, throwable)
}
