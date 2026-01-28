package com.christo.creditagricole.presentation.features.play

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.christo.creditagricole.designsystem.components.NavigationTopBar
import com.christo.creditagricole.presentation.features.navigation.MainBottomBar
import com.christo.creditagricole.presentation.features.navigation.MainBottomBarDestination

@Composable
fun PlayScreen(
    onSelectBottomDestination: (MainBottomBarDestination) -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant

    Scaffold(
        topBar = {
            NavigationTopBar(
                title = "À vous de jouer",
                containerColor = backgroundColor
            )
        },
        containerColor = backgroundColor,
        bottomBar = {
            MainBottomBar(
                selected = MainBottomBarDestination.Play,
                onSelected = onSelectBottomDestination
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Contenu À vous de jouer à venir")
        }
    }
}
