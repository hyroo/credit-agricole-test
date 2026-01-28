package com.christo.creditagricole.presentation.features.accounts

import com.christo.creditagricole.base.MviEffect
import com.christo.creditagricole.base.MviIntent
import com.christo.creditagricole.base.MviState
import com.christo.creditagricole.domain.model.Account
import com.christo.creditagricole.domain.model.AccountId
import com.christo.creditagricole.domain.model.BankId
import com.christo.creditagricole.domain.model.Money

data class AccountListState(
    val isLoading: Boolean = false,
    val hasLoaded: Boolean = false,
    val errorMessage: String? = null,
    val sections: List<BankSectionUi> = emptyList(),
    val navigationTarget: AccountListNavigation? = null
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
    val accountsError: String?,
    val totalBalance: Money? = null
)

data class AccountItemUi(
    val id: AccountId,
    val title: String,
    val account: Account
)

sealed interface AccountListNavigation {
    data class ToAccountDetail(val bankName: String, val account: Account) : AccountListNavigation
}

sealed interface AccountListIntent : MviIntent {
    data object OnAppear : AccountListIntent
    data object OnRefresh : AccountListIntent
    data class OnBankToggled(val bankId: BankId) : AccountListIntent
    data class OnAccountSelected(
        val bankId: BankId,
        val bankName: String,
        val account: AccountItemUi
    ) : AccountListIntent

    data object OnNavigationConsumed : AccountListIntent

    data object InternalLoading : AccountListIntent
    data class InternalBanksLoaded(
        val sections: List<BankSectionUi>,
        val markLoaded: Boolean
    ) : AccountListIntent

    data class InternalError(val message: String) : AccountListIntent
    data class InternalAccountsLoading(val bankId: BankId) : AccountListIntent
    data class InternalAccountsLoaded(val bankId: BankId, val accounts: List<AccountItemUi>) :
        AccountListIntent

    data class InternalAccountsError(val bankId: BankId, val message: String) : AccountListIntent
}

sealed interface AccountListResult {
    data object Idle : AccountListResult
    data object Loading : AccountListResult
    data class BanksContent(
        val sections: List<BankSectionUi>,
        val markLoaded: Boolean
    ) : AccountListResult

    data class Error(val message: String) : AccountListResult
    data class ToggleExpanded(val bankId: BankId) : AccountListResult
    data class AccountsLoading(val bankId: BankId) : AccountListResult
    data class AccountsContent(val bankId: BankId, val accounts: List<AccountItemUi>) :
        AccountListResult

    data class AccountsError(val bankId: BankId, val message: String) : AccountListResult
    data class NavigateToAccountDetail(val bankName: String, val account: Account) :
        AccountListResult

    data object NavigationConsumed : AccountListResult
}

sealed interface BankListEffect : MviEffect {
    data class ShowError(val message: String) : BankListEffect
}
