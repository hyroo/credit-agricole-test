package com.christo.creditagricole

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.christo.creditagricole.designsystem.theme.Theme
import com.christo.creditagricole.presentation.features.accounts.AccountListScreen
import com.christo.creditagricole.presentation.features.accounts.AccountListViewModel
import com.christo.creditagricole.presentation.features.navigation.NavigationTarget
import com.christo.creditagricole.presentation.features.navigation.rememberNavigator
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    Theme {
        KoinContext {
            val navigator = rememberNavigator<NavigationTarget>(NavigationTarget.BankList)
            when (val destination = navigator.current) {
                NavigationTarget.BankList -> {
                    val viewModel: AccountListViewModel = koinInject()
                    AccountListScreen(
                        viewModel = viewModel,
                        onNavigateToAccountDetail = { bankName, account ->
                            navigator.navigate(
                                NavigationTarget.AccountDetail(
                                    bankName = bankName,
                                    accountId = account.id
                                )
                            )
                        }
                    )
                }

                is NavigationTarget.AccountDetail -> {
                }
            }
        }
    }
}