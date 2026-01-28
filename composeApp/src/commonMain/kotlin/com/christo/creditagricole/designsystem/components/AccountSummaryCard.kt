package com.christo.creditagricole.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import com.christo.creditagricole.designsystem.theme.ThemeDefaults

@Composable
fun AccountSummaryCard(
    balanceText: String,
    accountKindText: String,
    modifier: Modifier = Modifier,
    headlineFontWeight: FontWeight = FontWeight.Bold
) {
    val d = ThemeDefaults.dimens
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = Color.Transparent,
        tonalElevation = d.spacingNone,
        shadowElevation = d.spacingNone
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = d.spacing24, horizontal = d.spacing20),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(d.spacing8)
        ) {
            Text(
                text = balanceText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = headlineFontWeight,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = accountKindText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
