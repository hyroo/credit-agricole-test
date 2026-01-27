package com.christo.creditagricole.domain.usecase

import com.christo.creditagricole.domain.NoParamSuspendUseCase
import com.christo.creditagricole.domain.model.Bank
import com.christo.creditagricole.domain.repository.BankRepository

class GetMockBanksUseCase(
    private val bankRepository: BankRepository
) : NoParamSuspendUseCase<List<Bank>> {

    override suspend fun invoke(params: Unit): List<Bank> {
        return bankRepository
            .getMockBanks()
            .sortedBy { it.name.lowercase() }
    }
}
