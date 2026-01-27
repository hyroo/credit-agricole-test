package com.christo.creditagricole.presentation.features.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * Very small navigation helper backed by a snapshot state back stack.
 * Works on every Compose Multiplatform target (Android/iOS/Desktop).
 */
@Stable
class Navigator<T> internal constructor(
    private val backStack: SnapshotStateList<T>
) {
    val current: T
        get() = backStack.last()

    fun navigate(destination: T) {
        backStack.add(destination)
    }

    fun pop(): Boolean {
        if (backStack.size <= 1) return false
        backStack.removeAt(backStack.lastIndex)
        return true
    }
}

@Composable
fun <T> rememberNavigator(initial: T): Navigator<T> {
    val backStack = remember { mutableStateListOf(initial) }
    return remember { Navigator(backStack) }
}
