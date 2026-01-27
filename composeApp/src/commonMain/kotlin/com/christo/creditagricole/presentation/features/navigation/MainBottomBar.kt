package com.christo.creditagricole.presentation.features.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.christo.creditagricole.designsystem.components.BottomBar
import com.christo.creditagricole.designsystem.components.BottomBarItem

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
    val items = remember {
        listOf(
            BottomBarItem(
                id = MainBottomBarDestination.Accounts,
                label = "Mes Comptes",
                icon = "M"
            ),
            BottomBarItem(
                id = MainBottomBarDestination.Simulation,
                label = "Simulation",
                icon = "S"
            ),
            BottomBarItem(
                id = MainBottomBarDestination.Play,
                label = "À vous de jouer",
                icon = "A"
            )
        )
    }

    BottomBar(
        items = items,
        selected = selected,
        onSelected = onSelected
    )
}
