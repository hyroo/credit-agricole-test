package com.christo.creditagricole.presentation.features.navigation

import com.christo.creditagricole.domain.model.AccountId


sealed interface NavigationTarget {
    data object BankList : NavigationTarget
    data class AccountDetail(
        val bankName: String,
        val accountId: AccountId
    ) : NavigationTarget
}
