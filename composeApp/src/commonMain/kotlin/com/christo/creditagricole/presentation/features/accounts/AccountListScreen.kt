package com.christo.creditagricole.presentation.features.accounts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.christo.creditagricole.designsystem.components.AccountListItem
import com.christo.creditagricole.designsystem.components.BankAccountsCard
import com.christo.creditagricole.designsystem.components.Divider
import com.christo.creditagricole.designsystem.components.NavigationTopBar
import com.christo.creditagricole.designsystem.components.SectionTitle
import com.christo.creditagricole.designsystem.theme.Theme
import com.christo.creditagricole.designsystem.theme.ThemeDefaults
import com.christo.creditagricole.domain.model.Account
import com.christo.creditagricole.domain.model.AccountId
import com.christo.creditagricole.domain.model.AccountKind
import com.christo.creditagricole.domain.model.BankId
import com.christo.creditagricole.domain.model.Money
import com.christo.creditagricole.presentation.features.navigation.MainBottomBar
import com.christo.creditagricole.presentation.features.navigation.MainBottomBarDestination
import com.christo.creditagricole.presentation.utils.formatAsCurrency
import creditagricole.composeapp.generated.resources.Res
import creditagricole.composeapp.generated.resources.account_list_empty_accounts
import creditagricole.composeapp.generated.resources.account_list_empty_banks
import creditagricole.composeapp.generated.resources.account_list_title
import creditagricole.composeapp.generated.resources.common_retry
import org.jetbrains.compose.resources.stringResource

@Composable
fun AccountListScreen(
    viewModel: AccountListViewModel,
    onNavigateToAccountDetail: (bankName: String, account: Account) -> Unit,
    onSelectBottomDestination: (MainBottomBarDestination) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.dispatch(AccountListIntent.OnAppear)
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is BankListEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    LaunchedEffect(state.navigationTarget) {
        val target = state.navigationTarget ?: return@LaunchedEffect
        viewModel.dispatch(AccountListIntent.OnNavigationConsumed)
        when (target) {
            is AccountListNavigation.ToAccountDetail -> onNavigateToAccountDetail(
                target.bankName,
                target.account
            )
        }
    }

    Scaffold(
        topBar = {
            NavigationTopBar(
                title = stringResource(Res.string.account_list_title),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            MainBottomBar(
                selected = MainBottomBarDestination.Accounts,
                onSelected = onSelectBottomDestination
            )
        }
    ) { padding ->
        BankListContent(
            state = state,
            padding = padding,
            onRefresh = { viewModel.dispatch(AccountListIntent.OnRefresh) },
            onBankToggled = { viewModel.dispatch(AccountListIntent.OnBankToggled(it)) },
            onAccountSelected = { bankId, bankName, account ->
                viewModel.dispatch(
                    AccountListIntent.OnAccountSelected(
                        bankId = bankId,
                        bankName = bankName,
                        account = account
                    )
                )
            }
        )
    }
}

@Composable
private fun BankListContent(
    state: AccountListState,
    padding: PaddingValues,
    onRefresh: () -> Unit,
    onBankToggled: (BankId) -> Unit,
    onAccountSelected: (bankId: BankId, bankName: String, account: AccountItemUi) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        when {
            !state.hasLoaded && state.errorMessage == null && state.sections.isEmpty() -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            state.isLoading && state.sections.isEmpty() -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            state.sections.isEmpty() -> {
                EmptyBanks(
                    errorMessage = state.errorMessage,
                    onRefresh = onRefresh,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                val d = ThemeDefaults.dimens
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = d.spacing16, horizontal = d.spacing16)
                ) {
                    state.sections.forEach { section ->
                        if (section.banks.isNotEmpty()) {
                            item(key = "${section.title}_header") {
                                SectionHeader(
                                    title = section.title,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(d.spacing12))
                            }
                            items(section.banks, key = { it.id.value }) { bank ->
                                BankCollapsibleCell(
                                    bank = bank,
                                    onToggle = { onBankToggled(bank.id) },
                                    onAccountSelected = { account ->
                                        onAccountSelected(bank.id, bank.title, account)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = d.spacing8)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Loading", showBackground = true)
@Composable
private fun AccountListLoadingPreview() {
    Theme {
        BankListContent(
            state = AccountListState(isLoading = true),
            padding = PaddingValues(),
            onRefresh = {},
            onBankToggled = {},
            onAccountSelected = { _, _, _ -> }
        )
    }
}

@Preview(name = "Content", showBackground = true)
@Composable
private fun AccountListContentPreview() {
    Theme {
        BankListContent(
            state = sampleContentState(),
            padding = PaddingValues(),
            onRefresh = {},
            onBankToggled = {},
            onAccountSelected = { _, _, _ -> }
        )
    }
}

@Preview(name = "Empty Error", showBackground = true)
@Composable
private fun AccountListEmptyPreview() {
    Theme {
        BankListContent(
            state = AccountListState(
                hasLoaded = true,
                errorMessage = "Impossible de charger les banques",
                sections = emptyList()
            ),
            padding = PaddingValues(),
            onRefresh = {},
            onBankToggled = {},
            onAccountSelected = { _, _, _ -> }
        )
    }
}

private fun sampleContentState(): AccountListState {
    val caBankId = BankId("ca-paris")
    val caAccounts = listOf(
        sampleAccountItem(
            id = "acc-1",
            bankId = caBankId,
            title = "Compte Courant",
            amountMinor = 152_340
        ),
        sampleAccountItem(
            id = "acc-2",
            bankId = caBankId,
            title = "Livret A",
            amountMinor = 12_500
        )
    )
    val otherBankId = BankId("bnpp")
    val otherAccounts = listOf(
        sampleAccountItem(
            id = "acc-3",
            bankId = otherBankId,
            title = "Compte BNP",
            amountMinor = 89_000
        )
    )
    val caSection = BankSectionUi(
        title = "Banques Crédit Agricole",
        banks = listOf(
            AccountCellUi(
                id = caBankId,
                title = "Crédit Agricole Paris",
                isCreditAgricole = true,
                isExpanded = true,
                isLoadingAccounts = false,
                accounts = caAccounts,
                accountsError = null,
                totalBalance = Money(164_840, "EUR", currencySymbolOverride = "€")
            )
        )
    )
    val otherSection = BankSectionUi(
        title = "Autres banques",
        banks = listOf(
            AccountCellUi(
                id = otherBankId,
                title = "BNP Paribas",
                isCreditAgricole = false,
                isExpanded = false,
                isLoadingAccounts = false,
                accounts = otherAccounts,
                accountsError = null,
                totalBalance = Money(89_000, "EUR", currencySymbolOverride = "€")
            )
        )
    )
    return AccountListState(
        hasLoaded = true,
        sections = listOf(caSection, otherSection)
    )
}

private fun sampleAccountItem(
    id: String,
    bankId: BankId,
    title: String,
    amountMinor: Long
): AccountItemUi = AccountItemUi(
    id = AccountId(id),
    title = title,
    account = Account(
        id = AccountId(id),
        bankId = bankId,
        name = title,
        balance = Money(amountMinor, "EUR", currencySymbolOverride = "€"),
        kind = AccountKind.CHECKING
    )
)

@Composable
private fun EmptyBanks(
    errorMessage: String?,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val d = ThemeDefaults.dimens
    Column(
        modifier = modifier.padding(d.spacing24),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val message = errorMessage ?: stringResource(Res.string.account_list_empty_banks)
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
        Button(
            onClick = onRefresh,
            modifier = Modifier.padding(top = d.spacing16)
        ) {
            Text(stringResource(Res.string.common_retry))
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    SectionTitle(
        text = title,
        modifier = modifier
    )
}

@Composable
private fun BankCollapsibleCell(
    bank: AccountCellUi,
    onToggle: () -> Unit,
    onAccountSelected: (AccountItemUi) -> Unit,
    modifier: Modifier = Modifier
) {
    BankAccountsCard(
        title = bank.title,
        balanceText = bank.totalBalance?.formatAsCurrency(),
        expanded = bank.isExpanded,
        onToggle = onToggle,
        modifier = modifier
    ) {
        val d = ThemeDefaults.dimens
        when {
            bank.isLoadingAccounts -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(top = d.spacing12)
                        .align(Alignment.CenterHorizontally),
                    strokeWidth = d.strokeThin
                )
            }

            bank.accountsError != null -> {
                Text(
                    text = bank.accountsError,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            bank.accounts.isEmpty() -> {
                Text(
                    text = stringResource(Res.string.account_list_empty_accounts),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> {
                bank.accounts.forEachIndexed { index, account ->
                    if (index > 0) {
                        Divider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = d.spacing8)
                        )
                    }
                    AccountListItem(
                        title = account.title,
                        balanceText = account.account.balance.formatAsCurrency(),
                        onClick = { onAccountSelected(account) },
                        modifier = Modifier.padding(vertical = d.spacing4)
                    )
                }
            }
        }
    }
}
