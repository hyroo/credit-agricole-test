package com.christo.creditagricole.domain.model

/**
 * Regroupe un compte et ses opérations associées.
 */
data class AccountWithOperations(
    val account: Account,
    val operations: List<Operation>
)

/**
 * Regroupe une banque et ses comptes.
 */
data class BankAccounts(
    val bank: Bank,
    val accounts: List<AccountWithOperations>,
    val totalBalance: Money
)

/**
 * Photographie complète du portefeuille de comptes.
 */
data class AccountsSnapshot(
    val banks: List<BankAccounts>
) {
    companion object {
        val Empty = AccountsSnapshot(emptyList())
    }
}

/**
 * Résultat du cas d'usage de consultation des comptes.
 */
data class AccountsOverview(
    val creditAgricoleAccounts: List<AccountWithOperations>,
    val otherBanks: List<BankAccounts>
)
