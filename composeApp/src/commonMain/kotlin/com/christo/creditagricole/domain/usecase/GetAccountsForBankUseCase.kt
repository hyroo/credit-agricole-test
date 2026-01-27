package com.christo.creditagricole.domain.usecase

import com.christo.creditagricole.domain.SuspendUseCase
import com.christo.creditagricole.domain.model.Account
import com.christo.creditagricole.domain.model.BankId
import com.christo.creditagricole.domain.repository.AccountRepository

class GetAccountsForBankUseCase(
    private val accountRepository: AccountRepository
) : SuspendUseCase<GetAccountsForBankUseCase.Params, List<Account>> {

    data class Params(val bankId: BankId)

    override suspend fun invoke(params: Params): List<Account> {
        return accountRepository
            .getAccounts(params.bankId)
            .sortedWith(compareBy(Account::kind, { it.name.lowercase() }))
    }
}
