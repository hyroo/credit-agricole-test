package com.christo.creditagricole.presentation.features.accounts

import BaseMVIViewModel
import com.christo.creditagricole.core.DispatcherProvider
import com.christo.creditagricole.domain.model.Account
import com.christo.creditagricole.domain.model.Bank
import com.christo.creditagricole.domain.model.BankId
import com.christo.creditagricole.domain.usecase.GetAccountsForBankUseCase
import com.christo.creditagricole.domain.usecase.GetBanksUseCase
import com.christo.creditagricole.domain.usecase.GetMockBanksUseCase
import creditagricole.composeapp.generated.resources.Res
import creditagricole.composeapp.generated.resources.account_list_empty_banks
import creditagricole.composeapp.generated.resources.account_list_generic_error
import creditagricole.composeapp.generated.resources.account_list_load_accounts_error
import creditagricole.composeapp.generated.resources.account_list_section_credit_agricole
import creditagricole.composeapp.generated.resources.account_list_section_others
import kotlinx.coroutines.CancellationException
import org.jetbrains.compose.resources.getString

class AccountListViewModel(
    dispatcherProvider: DispatcherProvider,
    private val getBanksUseCase: GetBanksUseCase,
    private val getMockBanksUseCase: GetMockBanksUseCase,
    private val getAccountsForBankUseCase: GetAccountsForBankUseCase
) : BaseMVIViewModel<AccountListIntent, AccountListState, AccountListResult, BankListEffect>(
    initialState = AccountListState(),
    reducer = AccountListReducer,
    dispatcherProvider = dispatcherProvider
) {

    override suspend fun executeIntent(intent: AccountListIntent): AccountListResult = when (intent) {
        AccountListIntent.OnAppear,
        AccountListIntent.OnRefresh -> {
            loadBanks(forceRefresh = true)
            AccountListResult.Idle
        }

        is AccountListIntent.OnBankToggled -> handleBankToggle(intent.bankId)

        is AccountListIntent.OnAccountSelected -> {
            AccountListResult.NavigateToAccountDetail(
                bankName = intent.bankName,
                account = intent.account.account
            )
        }

        AccountListIntent.OnNavigationConsumed -> AccountListResult.NavigationConsumed

        AccountListIntent.InternalLoading -> AccountListResult.Loading

        is AccountListIntent.InternalBanksLoaded -> AccountListResult.BanksContent(
            sections = intent.sections,
            markLoaded = intent.markLoaded
        )

        is AccountListIntent.InternalError -> AccountListResult.Error(intent.message)

        is AccountListIntent.InternalAccountsLoading -> AccountListResult.AccountsLoading(intent.bankId)

        is AccountListIntent.InternalAccountsLoaded -> AccountListResult.AccountsContent(
            bankId = intent.bankId,
            accounts = intent.accounts
        )

        is AccountListIntent.InternalAccountsError -> AccountListResult.AccountsError(
            bankId = intent.bankId,
            message = intent.message
        )
    }

    override suspend fun onEffect(result: AccountListResult): BankListEffect? = when (result) {
        is AccountListResult.Error -> BankListEffect.ShowError(result.message)
        is AccountListResult.AccountsError -> BankListEffect.ShowError(result.message)
        else -> null
    }

    private suspend fun loadBanks(forceRefresh: Boolean) {
        if (state.value.isLoading) return
        if (!forceRefresh && state.value.hasLoaded) return

        dispatch(AccountListIntent.InternalLoading)

        val emptyBanksMessage = getString(Res.string.account_list_empty_banks)
        val genericErrorMessage = getString(Res.string.account_list_generic_error)
        val creditAgricoleTitle = getString(Res.string.account_list_section_credit_agricole)
        val othersTitle = getString(Res.string.account_list_section_others)

        val currentSections = state.value.sections
        val fallbackSections = runCatching { getMockBanksUseCase() }
            .map { banks ->
                banks.toSections(currentSections, creditAgricoleTitle, othersTitle)
            }
            .getOrElse { emptyList() }

        if (fallbackSections.isNotEmpty() && currentSections.isEmpty()) {
            handleSectionsLoaded(
                sections = fallbackSections,
                markLoaded = true
            )
        }

        val remoteSectionsResult = runCatching { getBanksUseCase() }
            .map { banks ->
                banks.toSections(state.value.sections, creditAgricoleTitle, othersTitle)
            }

        remoteSectionsResult.onSuccess { sections ->
            val filteredSections = sections.filterNot { it.banks.isEmpty() }
            if (filteredSections.isEmpty()) {
                if (fallbackSections.isNotEmpty()) {
                    dispatch(
                        AccountListIntent.InternalBanksLoaded(
                            sections = fallbackSections,
                            markLoaded = true
                        )
                    )
                } else {
                    dispatch(AccountListIntent.InternalError(emptyBanksMessage))
                }
            } else {
                handleSectionsLoaded(
                    sections = filteredSections,
                    markLoaded = true
                )
            }
        }

        remoteSectionsResult.onFailure { throwable ->
            if (fallbackSections.isNotEmpty()) {
                handleSectionsLoaded(
                    sections = fallbackSections,
                    markLoaded = true
                )
            } else {
                val message =
                    throwable.message.orEmpty().ifEmpty { genericErrorMessage }
                dispatch(AccountListIntent.InternalError(message))
            }
        }
    }

    private suspend fun handleBankToggle(bankId: BankId): AccountListResult {
        val bankCell = state.value.sections
            .flatMap(BankSectionUi::banks)
            .firstOrNull { it.id == bankId } ?: return AccountListResult.Idle

        return if (bankCell.isExpanded) {
            AccountListResult.ToggleExpanded(bankId)
        } else {
            if (bankCell.accounts.isEmpty()) {
                dispatch(AccountListIntent.InternalAccountsLoading(bankId))
                fetchAccounts(bankId)
                AccountListResult.Idle
            } else {
                AccountListResult.ToggleExpanded(bankId)
            }
        }
    }

    private suspend fun fetchAccounts(bankId: BankId) {
        val result = runCatching {
            getAccountsForBankUseCase(
                GetAccountsForBankUseCase.Params(bankId = bankId)
            )
        }

        result.onSuccess { accounts ->
            val accountItems = accounts
                .sortedBy { it.name.lowercase() }
                .map { it.toUi() }
            dispatch(
                AccountListIntent.InternalAccountsLoaded(
                    bankId = bankId,
                    accounts = accountItems
                )
            )
        }

        if (result.isFailure) {
            val throwable = result.exceptionOrNull()
            val fallback = getString(Res.string.account_list_load_accounts_error)
            val message = throwable?.message.orEmpty().ifEmpty { fallback }
            dispatch(
                AccountListIntent.InternalAccountsError(
                    bankId = bankId,
                    message = message
                )
            )
        }
    }

    private fun List<Bank>.toSections(
        existingSections: List<BankSectionUi>,
        creditAgricoleTitle: String,
        othersTitle: String
    ): List<BankSectionUi> {
        val existingBanks = existingSections
            .flatMap { it.banks }
            .associateBy { it.id }

        val (creditAgricole, others) = partition { it.isCreditAgricole }

        fun buildSection(title: String, source: List<Bank>): BankSectionUi? {
            if (source.isEmpty()) return null
            val banks = source
                .sortedBy { it.name.lowercase() }
                .map { bank ->
                    val existing = existingBanks[bank.id]
                    AccountCellUi(
                        id = bank.id,
                        title = bank.name,
                        isCreditAgricole = bank.isCreditAgricole,
                        isExpanded = existing?.isExpanded ?: false,
                        isLoadingAccounts = existing?.isLoadingAccounts ?: false,
                        accounts = existing?.accounts ?: emptyList(),
                        accountsError = existing?.accountsError,
                        totalBalance = existing?.totalBalance
                    )
                }
            return BankSectionUi(title = title, banks = banks)
        }

        return buildList {
            buildSection(creditAgricoleTitle, creditAgricole)?.let(::add)
            buildSection(othersTitle, others)?.let(::add)
        }
    }

    private fun Account.toUi(): AccountItemUi = AccountItemUi(
        id = id,
        title = name,
        account = this
    )

    private suspend fun handleSectionsLoaded(
        sections: List<BankSectionUi>,
        markLoaded: Boolean
    ) {
        dispatch(
            AccountListIntent.InternalBanksLoaded(
                sections = sections,
                markLoaded = markLoaded
            )
        )
        prefetchBankAccountsForTotals(sections)
    }

    private suspend fun prefetchBankAccountsForTotals(sections: List<BankSectionUi>) {
        sections
            .flatMap { it.banks }
            .forEach { bank ->
                val existingBank = state.value.sections
                    .flatMap { it.banks }
                    .firstOrNull { it.id == bank.id }
                val hasAccountsLoaded = existingBank?.accounts?.isNotEmpty() == true
                val hasTotal = existingBank?.totalBalance != null
                val isLoading = existingBank?.isLoadingAccounts == true
                if (hasAccountsLoaded || hasTotal || isLoading) return@forEach

                runCatching {
                    getAccountsForBankUseCase(
                        GetAccountsForBankUseCase.Params(bankId = bank.id)
                    )
                }
                    .onSuccess { accounts ->
                        val accountItems = accounts
                            .sortedBy { it.name.lowercase() }
                            .map { it.toUi() }
                        dispatch(
                            AccountListIntent.InternalAccountsLoaded(
                                bankId = bank.id,
                                accounts = accountItems
                            )
                        )
                    }
                    .onFailure { throwable ->
                        if (throwable is CancellationException) throw throwable
                        // Silently ignore prefetch failures; totals will load on demand.
                    }
            }
    }
}
