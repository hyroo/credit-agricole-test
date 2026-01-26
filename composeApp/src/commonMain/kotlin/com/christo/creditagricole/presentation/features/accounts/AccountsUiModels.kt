package com.christo.creditagricole.presentation.features.accounts

data class AccountRowUi(
    val id: String,
    val title: String,
    val amountFormatted: String
)

data class BankAccountsGroupUi(
    val groupId: String,
    val title: String,
    val totalAmountFormatted: String,
    val expanded: Boolean,
    val accounts: List<AccountRowUi>
)