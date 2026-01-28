package com.christo.creditagricole.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import creditagricole.composeapp.generated.resources.Res
import creditagricole.composeapp.generated.resources.common_collapse
import creditagricole.composeapp.generated.resources.common_expand
import com.christo.creditagricole.designsystem.theme.ThemeDefaults
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

@Composable
fun BankAccountsCard(
    title: String,
    balanceText: String?,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val d = ThemeDefaults.dimens
    SurfaceCard(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        contentPadding = PaddingValues(d.spacingNone)
    ) {
        val collapseDescription = stringResource(Res.string.common_collapse)
        val expandDescription = stringResource(Res.string.common_expand)
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = d.spacing16, vertical = d.spacing16),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                balanceText?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) collapseDescription else expandDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (expanded) {
                Divider(modifier = Modifier.fillMaxWidth())
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = d.spacing16, vertical = d.spacing12),
                    content = content
                )
            }
        }
    }
}
