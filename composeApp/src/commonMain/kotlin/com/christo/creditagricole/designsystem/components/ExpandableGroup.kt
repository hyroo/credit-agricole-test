package com.christo.creditagricole.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.christo.creditagricole.designsystem.theme.ThemeDefaults

@Composable
fun ExpandableGroup(
    headerTitle: String,
    headerValue: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    headerTrailing: ListRowTrailing = if (expanded) ListRowTrailings.ChevronUp else ListRowTrailings.ChevronDown,
    content: @Composable () -> Unit
) {
    val d = ThemeDefaults.dimens

    Column(modifier = modifier) {
        ListRow(
            title = headerTitle,
            value = headerValue,
            onClick = onToggle,
            trailing = headerTrailing
        )
        if (expanded) {
            Spacer(Modifier.height(d.itemSpacing))
            content()
        }
    }
}