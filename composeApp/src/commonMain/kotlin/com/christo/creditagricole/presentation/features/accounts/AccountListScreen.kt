package com.christo.creditagricole.presentation.features.accounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import com.christo.creditagricole.designsystem.components.SectionTitle
import com.christo.creditagricole.domain.model.Account
import com.christo.creditagricole.domain.model.BankId
import com.christo.creditagricole.presentation.features.navigation.MainBottomBar
import com.christo.creditagricole.presentation.features.navigation.MainBottomBarDestination
import com.christo.creditagricole.presentation.utils.formatAsCurrency

@OptIn(ExperimentalMaterial3Api::class)
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
            TopAppBar(
                modifier = Modifier.height(72.dp),
                title = {
                    Text(
                        text = "Mes Comptes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp, bottom = 4.dp)
                                )
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
    Card(
        modifier = modifier
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = bank.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                bank.totalBalance?.let { money ->
                    Text(
                        text = money.formatAsCurrency(),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
                Icon(
                    imageVector = if (bank.isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (bank.isExpanded) "Réduire" else "Déplier"
                )
            }

            if (bank.isExpanded) {
                if (bank.isLoadingAccounts) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    when {
                        bank.accountsError != null -> {
                            Text(
                                text = bank.accountsError,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }

                        bank.accounts.isEmpty() -> {
                            Text(
                                text = "Aucun compte disponible.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }

                        else -> {
                            bank.accounts.forEachIndexed { index, account ->
                                HorizontalDivider(
                                    modifier = Modifier.padding(
                                        top = if (index == 0) 12.dp else 0.dp,
                                        bottom = 12.dp
                                    ),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(onClick = { onAccountSelected(account) })
                                        .padding(start = 16.dp, top = 6.dp, bottom = 6.dp)
                                ) {
                                    Text(
                                        text = account.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = account.account.balance.formatAsCurrency(),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.ChevronRight,
                                        contentDescription = "Consulter le détail",
                                        modifier = Modifier.padding(start = 12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
