package com.christo.creditagricole.presentation.features.accounts

import com.christo.creditagricole.base.MviReducer
import com.christo.creditagricole.domain.model.BankId

object AccountListReducer : MviReducer<AccountListState, BankListResult> {
    override fun reduce(currentState: AccountListState, result: BankListResult): AccountListState =
        when (result) {
            BankListResult.Idle -> currentState

            BankListResult.Loading -> currentState.copy(
                isLoading = true,
                errorMessage = null,
                navigationTarget = null
            )

            is BankListResult.BanksContent -> currentState.copy(
                isLoading = false,
                hasLoaded = currentState.hasLoaded || result.markLoaded,
                sections = result.sections,
                errorMessage = null,
                navigationTarget = null
            )

            is BankListResult.Error -> currentState.copy(
                isLoading = false,
                errorMessage = result.message
            )

            is BankListResult.ToggleExpanded -> currentState.copy(
                sections = currentState.sections.updateBank(result.bankId) { bank ->
                    bank.copy(
                        isExpanded = !bank.isExpanded,
                        accountsError = null
                    )
                }
            )

            is BankListResult.AccountsLoading -> currentState.copy(
                sections = currentState.sections.updateBank(result.bankId) { bank ->
                    bank.copy(
                        isExpanded = true,
                        isLoadingAccounts = true,
                        accountsError = null
                    )
                }
            )

            is BankListResult.AccountsContent -> currentState.copy(
                sections = currentState.sections.updateBank(result.bankId) { bank ->
                    bank.copy(
                        isExpanded = true,
                        isLoadingAccounts = false,
                        accounts = result.accounts,
                        accountsError = null
                    )
                }
            )

            is BankListResult.AccountsError -> currentState.copy(
                sections = currentState.sections.updateBank(result.bankId) { bank ->
                    bank.copy(
                        isLoadingAccounts = false,
                        accountsError = result.message
                    )
                }
            )

            is BankListResult.NavigateToAccountDetail -> currentState.copy(
                navigationTarget = BankListNavigation.ToAccountDetail(
                    bankName = result.bankName,
                    account = result.account
                )
            )

            BankListResult.NavigationConsumed -> currentState.copy(
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
