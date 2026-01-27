package com.christo.creditagricole.presentation.features.detail

import com.christo.creditagricole.base.MviEffect
import com.christo.creditagricole.base.MviIntent
import com.christo.creditagricole.base.MviState
import com.christo.creditagricole.domain.model.OperationId

data class AccountDetailState(
    val bankName: String,
    val accountName: String,
    val accountKind: String,
    val balance: String,
    val operations: List<OperationItemUi> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasLoaded: Boolean = false,
    val navigationTarget: AccountDetailNavigation? = null
) : MviState

data class OperationItemUi(
    val id: OperationId,
    val description: String,
    val amount: String,
    val typeLabel: String,
    val executedAt: String
)

sealed interface AccountDetailNavigation {
    data object Back : AccountDetailNavigation
}

sealed interface AccountDetailIntent : MviIntent {
    data object OnAppear : AccountDetailIntent
    data object OnRetry : AccountDetailIntent
    data object OnBackClicked : AccountDetailIntent
    data object OnNavigationConsumed : AccountDetailIntent

    data object InternalLoading : AccountDetailIntent
    data class InternalOperationsLoaded(val operations: List<OperationItemUi>) : AccountDetailIntent
    data class InternalError(val message: String) : AccountDetailIntent
}

sealed interface AccountDetailResult {
    data object Idle : AccountDetailResult
    data object Loading : AccountDetailResult
    data class OperationsContent(val operations: List<OperationItemUi>) : AccountDetailResult
    data class Error(val message: String) : AccountDetailResult
    data object NavigateBack : AccountDetailResult
    data object NavigationConsumed : AccountDetailResult
}

sealed interface AccountDetailEffect : MviEffect {
    data class ShowError(val message: String) : AccountDetailEffect
}
