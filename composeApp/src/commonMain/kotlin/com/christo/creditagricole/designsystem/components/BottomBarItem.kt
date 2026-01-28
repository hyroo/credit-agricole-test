package com.christo.creditagricole.designsystem.components

import androidx.compose.runtime.Immutable

@Immutable
data class BottomBarItem<T>(
    val id: T,
    val label: String,
    val icon: String
)
