package com.christo.creditagricole.presentation.features.detail

import BaseMVIViewModel
import com.christo.creditagricole.core.DispatcherProvider
import com.christo.creditagricole.domain.model.Account
import com.christo.creditagricole.domain.model.Money
import com.christo.creditagricole.domain.model.Operation
import com.christo.creditagricole.domain.model.OperationType
import com.christo.creditagricole.domain.usecase.GetOperationsForAccountUseCase
import kotlin.math.abs

class AccountDetailViewModel(
    dispatcherProvider: DispatcherProvider,
    bankName: String,
    account: Account,
    private val getOperationsForAccountUseCase: GetOperationsForAccountUseCase
) : BaseMVIViewModel<AccountDetailIntent, AccountDetailState, AccountDetailResult, AccountDetailEffect>(
    initialState = createInitialState(bankName, account),
    reducer = AccountDetailReducer,
    dispatcherProvider = dispatcherProvider
) {

    private val accountId = account.id

    override suspend fun executeIntent(intent: AccountDetailIntent): AccountDetailResult =
        when (intent) {
            AccountDetailIntent.OnAppear -> {
                if (state.value.operations.isEmpty()) {
                    loadOperations()
                }
                AccountDetailResult.Idle
            }

            AccountDetailIntent.OnRetry -> {
                loadOperations(force = true)
                AccountDetailResult.Idle
            }

            AccountDetailIntent.OnBackClicked -> AccountDetailResult.NavigateBack

            AccountDetailIntent.OnNavigationConsumed -> AccountDetailResult.NavigationConsumed

            AccountDetailIntent.InternalLoading -> AccountDetailResult.Loading

            is AccountDetailIntent.InternalOperationsLoaded -> AccountDetailResult.OperationsContent(
                intent.operations
            )

            is AccountDetailIntent.InternalError -> AccountDetailResult.Error(intent.message)
        }

    override suspend fun onEffect(result: AccountDetailResult): AccountDetailEffect? =
        when (result) {
            is AccountDetailResult.Error -> AccountDetailEffect.ShowError(result.message)
            else -> null
        }

    private suspend fun loadOperations(force: Boolean = false) {
        if (state.value.isLoading && !force) return

        dispatch(AccountDetailIntent.InternalLoading)
        runCatching {
            getOperationsForAccountUseCase(
                GetOperationsForAccountUseCase.Params(accountId = accountId)
            )
        }
            .map { operations ->
                operations
                    .sortedWith(
                        compareByDescending<Operation> { it.executedAtEpochMillis }
                            .thenBy { it.description.lowercase() }
                    )
                    .map { it.toUi() }
            }
            .onSuccess { operations ->
                dispatch(AccountDetailIntent.InternalOperationsLoaded(operations))
            }
            .onFailure { throwable ->
                val message =
                    throwable.message.orEmpty().ifEmpty { "Impossible de charger les operations." }
                dispatch(AccountDetailIntent.InternalError(message))
            }
    }

    private fun Operation.toUi(): OperationItemUi = OperationItemUi(
        id = id,
        description = description,
        amount = formatMoney(amount),
        typeLabel = when (type) {
            OperationType.CREDIT -> "Credit"
            OperationType.DEBIT -> "Debit"
        },
        executedAt = executedAtEpochMillis.toString()
    )

    companion object {
        private fun createInitialState(bankName: String, account: Account): AccountDetailState {
            val formattedBalance = formatMoney(
                account.balance
            )
            return AccountDetailState(
                bankName = bankName,
                accountName = account.name,
                accountKind = account.kind.name.lowercase().replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase() else it.toString()
                },
                balance = formattedBalance
            )
        }

        private fun formatMoney(money: Money): String {
            val absolute = abs(money.amountInMinor)
            val units = absolute / 100
            val cents = absolute % 100
            val centsString = cents.toString().padStart(2, '0')
            val sign = if (money.amountInMinor < 0) "-" else ""
            return "$sign$units.$centsString ${money.currencyCode}"
        }
    }
}
