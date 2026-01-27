package com.christo.creditagricole.data.repository

import com.christo.creditagricole.data.api.BankingApi
import com.christo.creditagricole.data.mapper.toDomain
import com.christo.creditagricole.domain.model.Account
import com.christo.creditagricole.domain.model.AccountId
import com.christo.creditagricole.domain.model.BankId
import com.christo.creditagricole.domain.repository.AccountRepository

class AccountRepositoryImpl(
    private val api: BankingApi
) : AccountRepository {

    override suspend fun getAccounts(bankId: BankId): List<Account> =
        runCatching {
            executeCall("Impossible de recuperer les comptes pour ${bankId.value}") {
                api.getAccountsForBank(bankId.value).accounts.map { it.toDomain(bankId) }
            }
        }.fold(
            onSuccess = { accounts -> accounts },
            onFailure = { throw it }
        )

    override suspend fun getAccount(accountId: AccountId): Account? =
        executeCall("Impossible de recuperer le compte ${accountId.value}") {
            api.getAccount(accountId.value).toDomain()
        }
}