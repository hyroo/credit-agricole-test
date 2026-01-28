package com.christo.creditagricole.presentation.features.navigation

import androidx.compose.runtime.Composable
import com.christo.creditagricole.designsystem.components.BottomBar
import com.christo.creditagricole.designsystem.components.BottomBarItem
import creditagricole.composeapp.generated.resources.Res
import creditagricole.composeapp.generated.resources.navigation_accounts
import creditagricole.composeapp.generated.resources.navigation_accounts_icon
import creditagricole.composeapp.generated.resources.navigation_play
import creditagricole.composeapp.generated.resources.navigation_play_icon
import creditagricole.composeapp.generated.resources.navigation_simulation
import creditagricole.composeapp.generated.resources.navigation_simulation_icon
import org.jetbrains.compose.resources.stringResource

enum class MainBottomBarDestination {
    Accounts,
    Simulation,
    Play
}

@Composable
fun MainBottomBar(
    selected: MainBottomBarDestination,
    onSelected: (MainBottomBarDestination) -> Unit
) {
    val items = listOf(
        BottomBarItem(
            id = MainBottomBarDestination.Accounts,
            label = stringResource(Res.string.navigation_accounts),
            icon = stringResource(Res.string.navigation_accounts_icon)
        ),
        BottomBarItem(
            id = MainBottomBarDestination.Simulation,
            label = stringResource(Res.string.navigation_simulation),
            icon = stringResource(Res.string.navigation_simulation_icon)
        ),
        BottomBarItem(
            id = MainBottomBarDestination.Play,
            label = stringResource(Res.string.navigation_play),
            icon = stringResource(Res.string.navigation_play_icon)
        )
    )

    BottomBar(
        items = items,
        selected = selected,
        onSelected = onSelected
    )
}
