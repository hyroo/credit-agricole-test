package com.christo.creditagricole

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.christo.creditagricole.designsystem.theme.Theme
import com.christo.creditagricole.presentation.features.accounts.AccountListScreen
import com.christo.creditagricole.presentation.features.accounts.AccountListViewModel
import com.christo.creditagricole.presentation.features.navigation.MainBottomBarDestination
import com.christo.creditagricole.presentation.features.detail.AccountDetailScreen
import com.christo.creditagricole.presentation.features.detail.AccountDetailViewModel
import com.christo.creditagricole.presentation.features.detail.AccountSelectionStore
import com.christo.creditagricole.presentation.features.navigation.NavigationTarget
import com.christo.creditagricole.presentation.features.navigation.rememberNavigator
import com.christo.creditagricole.presentation.features.play.PlayScreen
import com.christo.creditagricole.presentation.features.simulation.SimulationScreen
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
@Preview
fun App() {
    Theme {
        KoinContext {
            val navigator = rememberNavigator<NavigationTarget>(NavigationTarget.BankList)
            val onBottomDestinationSelected: (MainBottomBarDestination) -> Unit =
                on@{ destination ->
                    val target = when (destination) {
                        MainBottomBarDestination.Accounts -> NavigationTarget.BankList
                        MainBottomBarDestination.Simulation -> NavigationTarget.Simulation
                        MainBottomBarDestination.Play -> NavigationTarget.Play
                    }

                    if (navigator.current == target) return@on
                    navigator.replaceCurrent(target)
                }
            val accountSelectionStore: AccountSelectionStore = koinInject()
            when (val destination = navigator.current) {
                NavigationTarget.BankList -> {
                    val viewModel: AccountListViewModel = koinInject()
                    AccountListScreen(
                        viewModel = viewModel,
                        onNavigateToAccountDetail = { bankName, account ->
                            accountSelectionStore.put(account)
                            navigator.navigate(
                                NavigationTarget.AccountDetail(
                                    bankName = bankName,
                                    accountId = account.id
                                )
                            )
                        },
                        onSelectBottomDestination = onBottomDestinationSelected
                    )
                }

                NavigationTarget.Simulation -> {
                    SimulationScreen(
                        onSelectBottomDestination = onBottomDestinationSelected
                    )
                }

                NavigationTarget.Play -> {
                    PlayScreen(
                        onSelectBottomDestination = onBottomDestinationSelected
                    )
                }

                is NavigationTarget.AccountDetail -> {
                    val account = remember(destination.accountId) {
                        accountSelectionStore.consume(destination.accountId)
                    }
                    if (account == null) {
                        LaunchedEffect(destination.accountId) {
                            navigator.pop()
                        }
                    } else {
                        val viewModel: AccountDetailViewModel = koinInject(
                            parameters = { parametersOf(destination.bankName, account) }
                        )
                        AccountDetailScreen(
                            viewModel = viewModel,
                            onNavigateBack = {
                                navigator.pop()
                            }
                        )
                    }
                }
            }
        }
    }
}
