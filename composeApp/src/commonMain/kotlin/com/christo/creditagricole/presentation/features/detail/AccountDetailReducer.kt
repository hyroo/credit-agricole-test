package com.christo.creditagricole.presentation.features.detail

import com.christo.creditagricole.base.MviReducer


object AccountDetailReducer : MviReducer<AccountDetailState, AccountDetailResult> {
    override fun reduce(
        currentState: AccountDetailState,
        result: AccountDetailResult
    ): AccountDetailState =
        when (result) {
            AccountDetailResult.Idle -> currentState

            AccountDetailResult.Loading -> currentState.copy(
                isLoading = true,
                errorMessage = null,
                navigationTarget = null
            )

            is AccountDetailResult.OperationsContent -> currentState.copy(
                isLoading = false,
                operations = result.operations,
                errorMessage = null,
                hasLoaded = true
            )

            is AccountDetailResult.Error -> currentState.copy(
                isLoading = false,
                errorMessage = result.message,
                hasLoaded = true
            )

            AccountDetailResult.NavigateBack -> currentState.copy(
                navigationTarget = AccountDetailNavigation.Back
            )

            AccountDetailResult.NavigationConsumed -> currentState.copy(
                navigationTarget = null
            )
        }
}
