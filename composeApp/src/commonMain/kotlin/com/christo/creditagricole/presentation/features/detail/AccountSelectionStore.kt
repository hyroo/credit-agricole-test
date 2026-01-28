package com.christo.creditagricole.presentation.features.detail

import com.christo.creditagricole.domain.model.Account
import com.christo.creditagricole.domain.model.AccountId

class AccountSelectionStore {
    private val accounts = mutableMapOf<String, Account>()
    private val preloaded = mutableMapOf<String, Account>()

    fun put(account: Account) {
        accounts[account.id.value] = account
        preloaded[account.id.value] = account
    }

    fun consume(accountId: AccountId): Account? {
        val key = accountId.value
        val account = accounts.remove(key)
        if (account != null) {
            preloaded.remove(key)
            return account
        }
        return preloaded[key]
    }

    fun clear() {
        accounts.clear()
        preloaded.clear()
    }
}
