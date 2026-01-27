package com.christo.creditagricole.presentation.features.accounts

import ListSectionItems
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.christo.creditagricole.designsystem.components.BottomBar
import com.christo.creditagricole.designsystem.components.BottomBarItem
import com.christo.creditagricole.designsystem.components.ExpandableGroup
import com.christo.creditagricole.designsystem.components.ListRow
import com.christo.creditagricole.designsystem.components.ListRowTrailings
import com.christo.creditagricole.designsystem.components.SectionTitle
import com.christo.creditagricole.designsystem.components.TopTitle
import com.christo.creditagricole.designsystem.theme.ThemeDefaults

@Composable
fun AccountsRoute(
    state: AccountsState,
    onIntent: (AccountsIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomItems = listOf(
        BottomBarItem(AccountsTab.MES_COMPTES, "Mes Comptes", "★"),
        BottomBarItem(AccountsTab.SIMULATION, "Simulation", "★"),
        BottomBarItem(AccountsTab.A_VOUS_DE_JOUER, "à vous de jouer", "★")
    )

    Scaffold(
        containerColor = ThemeDefaults.colors.background ,
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomBar(
                items = bottomItems,
                selected = state.selectedTab,
                onSelected = { onIntent(AccountsIntent.TabSelected(it)) }
            )
        }
    ) { padding ->
        AccountsScreen(
            state = state,
            onIntent = onIntent,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun AccountsScreen(
    state: AccountsState,
    onIntent: (AccountsIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val d = ThemeDefaults.dimens

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = d.screenPadding, vertical = d.screenPadding),
        verticalArrangement = Arrangement.spacedBy(d.sectionSpacing)
    ) {
        item {
            TopTitle("Mes Comptes")
        }

//        items(state.creditAgricoleAccounts) { group ->
//            ListSectionItems(
//                title = "Credit Agricole",
//                items = group
//            ) { acc ->
//                ListRow(
//                    title = acc.title,
//                    value = acc.amountFormatted,
//                    onClick = { onIntent(AccountsIntent.AccountClicked(acc.id)) },
//                    trailing = ListRowTrailings.ChevronDown
//                )
//            }
//        }
        item {
            ListSectionItems(
                title = "Credit Agricole",
                items = state.creditAgricoleAccounts
            ) { acc ->
                ListRow(
                    title = acc.title,
                    value = acc.amountFormatted,
                    onClick = { onIntent(AccountsIntent.AccountClicked(acc.id)) },
                    trailing = ListRowTrailings.ChevronDown
                )
            }
        }

        item { SectionTitle("Autres Banques") }

        items(state.otherBanksGroups) { group ->
            ExpandableGroup(
                headerTitle = group.title,
                headerValue = group.totalAmountFormatted,
                expanded = group.expanded,
                onToggle = { onIntent(AccountsIntent.ToggleGroup(group.groupId)) }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(d.itemSpacing)) {
                    group.accounts.forEach { acc ->
                        ListRow(
                            title = acc.title,
                            value = acc.amountFormatted,
                            onClick = { onIntent(AccountsIntent.AccountClicked(acc.id)) },
                            titleIndentDp = 12,
                            trailing = ListRowTrailings.ChevronRight
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(64.dp)) }
    }
}
