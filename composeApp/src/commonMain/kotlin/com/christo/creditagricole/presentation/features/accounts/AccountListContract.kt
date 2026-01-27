package com.christo.creditagricole.presentation.features.accounts

import com.christo.creditagricole.base.MviEffect
import com.christo.creditagricole.base.MviIntent
import com.christo.creditagricole.base.MviState
import com.christo.creditagricole.domain.model.Account
import com.christo.creditagricole.domain.model.AccountId
import com.christo.creditagricole.domain.model.BankId

data class AccountListState(
    val isLoading: Boolean = false,
    val hasLoaded: Boolean = false,
    val errorMessage: String? = null,
    val sections: List<BankSectionUi> = emptyList(),
    val navigationTarget: BankListNavigation? = null
) : MviState

data class BankSectionUi(
    val title: String,
    val banks: List<AccountCellUi>
)

data class AccountCellUi(
    val id: BankId,
    val title: String,
    val isCreditAgricole: Boolean,
    val isExpanded: Boolean,
    val isLoadingAccounts: Boolean,
    val accounts: List<AccountItemUi>,
    val accountsError: String?
)

data class AccountItemUi(
    val id: AccountId,
    val title: String,
    val account: Account
)

sealed interface BankListNavigation {
    data class ToAccountDetail(val bankName: String, val account: Account) : BankListNavigation
}

sealed interface BankListIntent : MviIntent {
    data object OnAppear : BankListIntent
    data object OnRefresh : BankListIntent
    data class OnBankToggled(val bankId: BankId) : BankListIntent
    data class OnAccountSelected(
        val bankId: BankId,
        val bankName: String,
        val account: AccountItemUi
    ) : BankListIntent
    data object OnNavigationConsumed : BankListIntent

    data object InternalLoading : BankListIntent
    data class InternalBanksLoaded(
        val sections: List<BankSectionUi>,
        val markLoaded: Boolean
    ) : BankListIntent

    data class InternalError(val message: String) : BankListIntent
    data class InternalAccountsLoading(val bankId: BankId) : BankListIntent
    data class InternalAccountsLoaded(val bankId: BankId, val accounts: List<AccountItemUi>) : BankListIntent
    data class InternalAccountsError(val bankId: BankId, val message: String) : BankListIntent
}

sealed interface BankListResult {
    data object Idle : BankListResult
    data object Loading : BankListResult
    data class BanksContent(
        val sections: List<BankSectionUi>,
        val markLoaded: Boolean
    ) : BankListResult

    data class Error(val message: String) : BankListResult
    data class ToggleExpanded(val bankId: BankId) : BankListResult
    data class AccountsLoading(val bankId: BankId) : BankListResult
    data class AccountsContent(val bankId: BankId, val accounts: List<AccountItemUi>) : BankListResult
    data class AccountsError(val bankId: BankId, val message: String) : BankListResult
    data class NavigateToAccountDetail(val bankName: String, val account: Account) : BankListResult
    data object NavigationConsumed : BankListResult
}

sealed interface BankListEffect : MviEffect {
    data class ShowError(val message: String) : BankListEffect
}
