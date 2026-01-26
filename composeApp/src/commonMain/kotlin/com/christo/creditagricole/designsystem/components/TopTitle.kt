package com.christo.creditagricole.designsystem.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.christo.creditagricole.designsystem.theme.ThemeDefaults

@Composable
fun TopTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    val colors = ThemeDefaults.colors

    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.ExtraBold,
        color = colors.muted
    )
}