package com.christo.creditagricole

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.christo.creditagricole.designsystem.theme.ColorScheme
import com.christo.creditagricole.designsystem.theme.Theme
import com.christo.creditagricole.presentation.features.accounts.AccountsMock
import com.christo.creditagricole.presentation.features.accounts.AccountsRoute

@Composable
@Preview
fun App() {
    Theme(colors = ColorScheme.Light) {
        AccountsRoute(
            state = AccountsMock.defaultState,
            onIntent = {}
        )
    }
}