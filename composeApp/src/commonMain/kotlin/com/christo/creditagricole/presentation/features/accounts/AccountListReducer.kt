package com.christo.creditagricole.presentation.features.accounts

import com.christo.creditagricole.base.MviReducer
import com.christo.creditagricole.domain.model.BankId

object AccountListReducer : MviReducer<AccountListState, AccountListResult> {
    override fun reduce(currentState: AccountListState, result: AccountListResult): AccountListState =
        when (result) {
            AccountListResult.Idle -> currentState

            AccountListResult.Loading -> currentState.copy(
                isLoading = true,
                errorMessage = null,
                navigationTarget = null
            )

            is AccountListResult.BanksContent -> currentState.copy(
                isLoading = false,
                hasLoaded = currentState.hasLoaded || result.markLoaded,
                sections = result.sections,
                errorMessage = null,
                navigationTarget = null
            )

            is AccountListResult.Error -> currentState.copy(
                isLoading = false,
                errorMessage = result.message
            )

            is AccountListResult.ToggleExpanded -> currentState.copy(
                sections = currentState.sections.updateBank(result.bankId) { bank ->
                    bank.copy(
                        isExpanded = !bank.isExpanded,
                        accountsError = null
                    )
                }
            )

            is AccountListResult.AccountsLoading -> currentState.copy(
                sections = currentState.sections.updateBank(result.bankId) { bank ->
                    bank.copy(
                        isExpanded = true,
                        isLoadingAccounts = true,
                        accountsError = null
                    )
                }
            )

            is AccountListResult.AccountsContent -> currentState.copy(
                sections = currentState.sections.updateBank(result.bankId) { bank ->
                    bank.copy(
                        isExpanded = true,
                        isLoadingAccounts = false,
                        accounts = result.accounts,
                        accountsError = null
                    )
                }
            )

            is AccountListResult.AccountsError -> currentState.copy(
                sections = currentState.sections.updateBank(result.bankId) { bank ->
                    bank.copy(
                        isLoadingAccounts = false,
                        accountsError = result.message
                    )
                }
            )

            is AccountListResult.NavigateToAccountDetail -> currentState.copy(
                navigationTarget = AccountListNavigation.ToAccountDetail(
                    bankName = result.bankName,
                    account = result.account
                )
            )

            AccountListResult.NavigationConsumed -> currentState.copy(
                navigationTarget = null
            )
        }
}

private fun List<BankSectionUi>.updateBank(
    bankId: BankId,
    transform: (AccountCellUi) -> AccountCellUi
): List<BankSectionUi> =
    map { section ->
        val updatedBanks = section.banks.map { bank ->
            if (bank.id == bankId) transform(bank) else bank
        }
        section.copy(banks = updatedBanks)
    }
