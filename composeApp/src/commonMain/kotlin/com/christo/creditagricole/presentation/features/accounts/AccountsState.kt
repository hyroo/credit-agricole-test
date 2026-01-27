package com.christo.creditagricole.presentation.features.accounts

enum class AccountsTab { MES_COMPTES, SIMULATION, A_VOUS_DE_JOUER }

data class AccountsState(
    val isLoading: Boolean = false,
    val selectedTab: AccountsTab = AccountsTab.MES_COMPTES,

    val creditAgricoleAccounts: List<AccountRowUi> = emptyList(),
    val otherBanksGroups: List<BankAccountsGroupUi> = emptyList(),

    val errorMessage: String? = null
)

sealed interface AccountsIntent {
    data object Refresh : AccountsIntent
    data class TabSelected(val tab: AccountsTab) : AccountsIntent
    data class AccountClicked(val accountId: String) : AccountsIntent
    data class ToggleGroup(val groupId: String) : AccountsIntent
}

sealed interface AccountsEffect {
    data class NavigateToAccountDetail(val accountId: String) : AccountsEffect
    data class ShowToast(val message: String) : AccountsEffect
}