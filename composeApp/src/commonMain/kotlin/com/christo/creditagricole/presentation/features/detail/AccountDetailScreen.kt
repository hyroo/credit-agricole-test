package com.christo.creditagricole.presentation.features.detail

import androidx.compose.foundation.background
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
import com.christo.creditagricole.designsystem.components.AccountSummaryCard
import com.christo.creditagricole.designsystem.components.NavigationTopBar
import com.christo.creditagricole.designsystem.components.OperationCard
import com.christo.creditagricole.designsystem.theme.ThemeDefaults
import creditagricole.composeapp.generated.resources.Res
import creditagricole.composeapp.generated.resources.account_detail_empty_operations
import creditagricole.composeapp.generated.resources.account_detail_executed_on
import creditagricole.composeapp.generated.resources.account_detail_title
import creditagricole.composeapp.generated.resources.common_back
import creditagricole.composeapp.generated.resources.common_retry
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.ExperimentalResourceApi

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
                title = stringResource(Res.string.account_detail_title),
                onBackClick = { viewModel.dispatch(AccountDetailIntent.OnBackClicked) },
                navigationContentDescription = stringResource(Res.string.common_back)
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
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
    val d = ThemeDefaults.dimens
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = d.spacing16, vertical = d.spacing12)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Spacer(modifier = Modifier.height(d.spacing12))
        AccountSummaryCard(
            balanceText = state.balance,
            accountKindText = state.accountKind,
            modifier = Modifier.padding(horizontal = d.spacing16)
        )

        Spacer(modifier = Modifier.height(d.spacing16))

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
                    .padding(bottom = d.spacing8)
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
        val d = ThemeDefaults.dimens
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(d.spacing12)
        ) {
            Text(
                text = errorMessage ?: stringResource(Res.string.account_detail_empty_operations),
                style = MaterialTheme.typography.bodyLarge
            )
            Button(onClick = onRetry) {
                Text(text = stringResource(Res.string.common_retry))
            }
        }
    }
}

@Composable
private fun OperationsList(
    operations: List<OperationItemUi>,
    modifier: Modifier = Modifier
) {
    val d = ThemeDefaults.dimens
    if (operations.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(Res.string.account_detail_empty_operations),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(d.spacing12),
        contentPadding = PaddingValues(bottom = d.spacing24)
    ) {
        itemsIndexed(operations, key = { index, item -> "${item.id.value}_$index" }) { _, operation ->
            val dateText = stringResource(Res.string.account_detail_executed_on, operation.executedAt)
            val typeLabel = operation.typeLabelRes?.let { stringResource(it) }
            OperationCard(
                title = operation.description,
                amountText = operation.amount,
                dateText = dateText,
                typeLabel = typeLabel
            )
        }
    }
}
