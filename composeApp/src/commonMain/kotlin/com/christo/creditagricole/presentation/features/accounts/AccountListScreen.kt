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
import androidx.compose.ui.unit.dp
import com.christo.creditagricole.designsystem.components.AccountListItem
import com.christo.creditagricole.designsystem.components.BankAccountsCard
import com.christo.creditagricole.designsystem.components.Divider
import com.christo.creditagricole.designsystem.components.NavigationTopBar
import com.christo.creditagricole.designsystem.components.SectionTitle
import com.christo.creditagricole.domain.model.Account
import com.christo.creditagricole.domain.model.BankId
import com.christo.creditagricole.presentation.features.navigation.MainBottomBar
import com.christo.creditagricole.presentation.features.navigation.MainBottomBarDestination
import com.christo.creditagricole.presentation.utils.formatAsCurrency

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
                title = "Mes comptes",
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp)
                ) {
                    state.sections.forEach { section ->
                        if (section.banks.isNotEmpty()) {
                            item(key = "${section.title}_header") {
                                SectionHeader(
                                    title = section.title,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(12.dp))
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
                                        .padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyBanks(
    errorMessage: String?,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val message = errorMessage ?: "Aucune banque disponible."
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
        Button(
            onClick = onRefresh,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Reessayer")
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
        when {
            bank.isLoadingAccounts -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .align(Alignment.CenterHorizontally),
                    strokeWidth = 2.dp
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
                    text = "Aucun compte disponible.",
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
                                .padding(vertical = 8.dp)
                        )
                    }
                    AccountListItem(
                        title = account.title,
                        balanceText = account.account.balance.formatAsCurrency(),
                        onClick = { onAccountSelected(account) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
