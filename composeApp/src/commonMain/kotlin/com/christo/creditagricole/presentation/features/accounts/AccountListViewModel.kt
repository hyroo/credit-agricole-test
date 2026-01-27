package com.christo.creditagricole.presentation.features.accounts

import BaseMVIViewModel
import com.christo.creditagricole.core.DispatcherProvider
import com.christo.creditagricole.domain.model.Account
import com.christo.creditagricole.domain.model.Bank
import com.christo.creditagricole.domain.model.BankId
import com.christo.creditagricole.domain.usecase.GetAccountsForBankUseCase
import com.christo.creditagricole.domain.usecase.GetBanksUseCase
import com.christo.creditagricole.domain.usecase.GetMockBanksUseCase

class AccountListViewModel(
    dispatcherProvider: DispatcherProvider,
    private val getBanksUseCase: GetBanksUseCase,
    private val getMockBanksUseCase: GetMockBanksUseCase,
    private val getAccountsForBankUseCase: GetAccountsForBankUseCase
) : BaseMVIViewModel<BankListIntent, AccountListState, BankListResult, BankListEffect>(
    initialState = AccountListState(),
    reducer = AccountListReducer,
    dispatcherProvider = dispatcherProvider
) {

    override suspend fun executeIntent(intent: BankListIntent): BankListResult = when (intent) {
        BankListIntent.OnAppear,
        BankListIntent.OnRefresh -> {
            loadBanks(forceRefresh = true)
            BankListResult.Idle
        }

        is BankListIntent.OnBankToggled -> handleBankToggle(intent.bankId)

        is BankListIntent.OnAccountSelected -> {
            BankListResult.NavigateToAccountDetail(
                bankName = intent.bankName,
                account = intent.account.account
            )
        }

        BankListIntent.OnNavigationConsumed -> BankListResult.NavigationConsumed

        BankListIntent.InternalLoading -> BankListResult.Loading

        is BankListIntent.InternalBanksLoaded -> BankListResult.BanksContent(
            sections = intent.sections,
            markLoaded = intent.markLoaded
        )

        is BankListIntent.InternalError -> BankListResult.Error(intent.message)

        is BankListIntent.InternalAccountsLoading -> BankListResult.AccountsLoading(intent.bankId)

        is BankListIntent.InternalAccountsLoaded -> BankListResult.AccountsContent(
            bankId = intent.bankId,
            accounts = intent.accounts
        )

        is BankListIntent.InternalAccountsError -> BankListResult.AccountsError(
            bankId = intent.bankId,
            message = intent.message
        )
    }

    override suspend fun onEffect(result: BankListResult): BankListEffect? = when (result) {
        is BankListResult.Error -> BankListEffect.ShowError(result.message)
        is BankListResult.AccountsError -> BankListEffect.ShowError(result.message)
        else -> null
    }

    private suspend fun loadBanks(forceRefresh: Boolean) {
        if (state.value.isLoading) return
        if (!forceRefresh && state.value.hasLoaded) return

        dispatch(BankListIntent.InternalLoading)

        val currentSections = state.value.sections
        val fallbackSections = runCatching { getMockBanksUseCase() }
            .map { banks ->
                val sections = banks.toSections(currentSections)
                sections
            }
            .getOrElse { emptyList() }

        if (fallbackSections.isNotEmpty() && currentSections.isEmpty()) {
            dispatch(
                BankListIntent.InternalBanksLoaded(
                    sections = fallbackSections,
                    markLoaded = true
                )
            )
        }

        runCatching { getBanksUseCase() }
            .map { banks ->
                val sections = banks.toSections(state.value.sections)
                sections
            }
            .onSuccess { sections ->
                val filteredSections = sections.filterNot { it.banks.isEmpty() }
                if (filteredSections.isEmpty()) {
                    if (fallbackSections.isNotEmpty()) {
                        dispatch(
                            BankListIntent.InternalBanksLoaded(
                                sections = fallbackSections,
                                markLoaded = true
                            )
                        )
                    } else {
                        dispatch(BankListIntent.InternalError("Aucune banque disponible."))
                    }
                } else {
                    dispatch(
                        BankListIntent.InternalBanksLoaded(
                            sections = filteredSections,
                            markLoaded = true
                        )
                    )
                }
            }
            .onFailure { throwable ->
                if (fallbackSections.isNotEmpty()) {
                    dispatch(
                        BankListIntent.InternalBanksLoaded(
                            sections = fallbackSections,
                            markLoaded = true
                        )
                    )
                } else {
                    val message =
                        throwable.message.orEmpty().ifEmpty { "Une erreur s'est produite." }
                    dispatch(BankListIntent.InternalError(message))
                }
            }
    }

    private suspend fun handleBankToggle(bankId: BankId): BankListResult {
        val bankCell = state.value.sections
            .flatMap(BankSectionUi::banks)
            .firstOrNull { it.id == bankId } ?: return BankListResult.Idle

        return if (bankCell.isExpanded) {
            BankListResult.ToggleExpanded(bankId)
        } else {
            if (bankCell.accounts.isEmpty()) {
                dispatch(BankListIntent.InternalAccountsLoading(bankId))
                fetchAccounts(bankId)
                BankListResult.Idle
            } else {
                BankListResult.ToggleExpanded(bankId)
            }
        }
    }

    private suspend fun fetchAccounts(bankId: BankId) {
        runCatching {
            getAccountsForBankUseCase(
                GetAccountsForBankUseCase.Params(bankId = bankId)
            )
        }
            .onSuccess { accounts ->
                val accountItems = accounts
                    .sortedBy { it.name.lowercase() }
                    .map { it.toUi() }
                dispatch(
                    BankListIntent.InternalAccountsLoaded(
                        bankId = bankId,
                        accounts = accountItems
                    )
                )
            }
            .onFailure { throwable ->
                val message =
                    throwable.message.orEmpty().ifEmpty { "Impossible de charger les comptes." }
                dispatch(
                    BankListIntent.InternalAccountsError(
                        bankId = bankId,
                        message = message
                    )
                )
            }
    }

    private fun List<Bank>.toSections(existingSections: List<BankSectionUi>): List<BankSectionUi> {
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
                        accountsError = existing?.accountsError
                    )
                }
            return BankSectionUi(title = title, banks = banks)
        }

        return buildList {
            buildSection("Banques Crédit Agricole", creditAgricole)?.let(::add)
            buildSection("Autres banques", others)?.let(::add)
        }
    }

    private fun Account.toUi(): AccountItemUi = AccountItemUi(
        id = id,
        title = name,
        account = this
    )
}
