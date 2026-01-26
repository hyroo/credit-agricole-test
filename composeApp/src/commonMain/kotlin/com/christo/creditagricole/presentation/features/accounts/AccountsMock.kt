package com.christo.creditagricole.presentation.features.accounts

object AccountsMock {

    val defaultState = AccountsState(
        isLoading = false,
        selectedTab = AccountsTab.MES_COMPTES,
        creditAgricoleAccounts = listOf(
            AccountRowUi(
                id = "ca_1",
                title = "Compte Courant",
                amountFormatted = "2 345,20 €"
            ),
            AccountRowUi(
                id = "ca_2",
                title = "Livret A",
                amountFormatted = "8 120,00 €"
            )
        ),
        otherBanksGroups = listOf(
            BankAccountsGroupUi(
                groupId = "sg",
                title = "Société Générale",
                totalAmountFormatted = "4 820,00 €",
                expanded = true,
                accounts = listOf(
                    AccountRowUi(
                        id = "sg_1",
                        title = "Compte Principal",
                        amountFormatted = "1 230,00 €"
                    ),
                    AccountRowUi(
                        id = "sg_2",
                        title = "PEL",
                        amountFormatted = "3 590,00 €"
                    )
                )
            ),
            BankAccountsGroupUi(
                groupId = "bnpp",
                title = "BNP Paribas",
                totalAmountFormatted = "950,00 €",
                expanded = false,
                accounts = emptyList()
            )
        )
    )
}