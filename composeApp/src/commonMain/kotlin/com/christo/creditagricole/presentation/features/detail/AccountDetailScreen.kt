package com.christo.creditagricole.presentation.features.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.christo.creditagricole.designsystem.components.AccountSummaryCard
import com.christo.creditagricole.designsystem.components.NavigationTopBar
import com.christo.creditagricole.designsystem.components.OperationCard

@Composable
fun AccountDetailScreen(
    viewModel: AccountDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.dispatch(AccountDetailIntent.OnAppear)
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AccountDetailEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    LaunchedEffect(state.navigationTarget) {
        when (state.navigationTarget) {
            AccountDetailNavigation.Back -> {
                onNavigateBack()
                viewModel.dispatch(AccountDetailIntent.OnNavigationConsumed)
            }

            null -> Unit
        }
    }

    DisposableEffect(viewModel) {
        onDispose { viewModel.clear() }
    }

    Scaffold(
        topBar = {
            NavigationTopBar(
                title = "Mes comptes",
                onBackClick = { viewModel.dispatch(AccountDetailIntent.OnBackClicked) },
                navigationContentDescription = "Retour"
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        AccountDetailContent(
            state = state,
            padding = padding,
            onRetry = { viewModel.dispatch(AccountDetailIntent.OnRetry) }
        )
    }
}

@Composable
private fun AccountDetailContent(
    state: AccountDetailState,
    padding: PaddingValues,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        AccountSummaryCard(
            balanceText = state.balance,
            accountKindText = state.accountKind,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        val showSpinner =
            state.operations.isEmpty() && state.errorMessage == null && (!state.hasLoaded || state.isLoading)

        if (showSpinner) {
            Box(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return
        }

        if (state.operations.isEmpty() && !state.isLoading) {
            EmptyOperations(
                errorMessage = state.errorMessage,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize()
            )
            return
        }

        state.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        OperationsList(
            operations = state.operations,
            modifier = Modifier.weight(1f, fill = true)
        )
    }
}

@Composable
private fun EmptyOperations(
    errorMessage: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = errorMessage ?: "Aucune operation a afficher.",
                style = MaterialTheme.typography.bodyLarge
            )
            Button(onClick = onRetry) {
                Text(text = "Reessayer")
            }
        }
    }
}

@Composable
private fun OperationsList(
    operations: List<OperationItemUi>,
    modifier: Modifier = Modifier
) {
    if (operations.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Aucune operation a afficher.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        itemsIndexed(operations, key = { index, item -> "${item.id.value}_$index" }) { _, operation ->
            OperationCard(
                title = operation.description,
                amountText = operation.amount,
                dateText = "Le ${operation.executedAt}",
                typeLabel = operation.typeLabel
            )
        }
    }
}
