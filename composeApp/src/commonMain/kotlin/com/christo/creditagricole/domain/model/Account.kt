package com.christo.creditagricole.domain.model

data class Account(
    val id: AccountId,
    val bankId: BankId,
    val name: String,
    val balance: Money,
    val kind: AccountKind
) {
    init {
        require(name.isNotBlank()) { "Account name cannot be blank." }
    }
}

enum class AccountKind {
    CHECKING,
    SAVINGS,
    INVESTMENT,
    LOAN,
    OTHER
}
