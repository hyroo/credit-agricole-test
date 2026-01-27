package com.christo.creditagricole.designsystem.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun <T> BottomBar(
    items: List<BottomBarItem<T>>,
    selected: T,
    onSelected: (T) -> Unit
) {
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.id == selected,
                onClick = { onSelected(item.id) },
                icon = { Text(item.icon) },
                label = { Text(item.label) }
            )
        }
    }
}