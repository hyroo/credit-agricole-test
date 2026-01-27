package com.christo.creditagricole.presentation.features.simulation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.christo.creditagricole.presentation.features.navigation.MainBottomBar
import com.christo.creditagricole.presentation.features.navigation.MainBottomBarDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulationScreen(
    onSelectBottomDestination: (MainBottomBarDestination) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Simulation") }) },
        bottomBar = {
            MainBottomBar(
                selected = MainBottomBarDestination.Simulation,
                onSelected = onSelectBottomDestination
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Contenu Simulation à venir")
        }
    }
}
