package com.christo.creditagricole.domain.usecase

import com.christo.creditagricole.domain.NoParamSuspendUseCase
import com.christo.creditagricole.domain.model.Bank
import com.christo.creditagricole.domain.repository.BankRepository

class GetBanksUseCase(
    private val bankRepository: BankRepository
) : NoParamSuspendUseCase<List<Bank>> {

    override suspend fun invoke(params: Unit): List<Bank> {
        return bankRepository
            .getBanks()
            .sortedBy { it.name.lowercase() }
    }
}
