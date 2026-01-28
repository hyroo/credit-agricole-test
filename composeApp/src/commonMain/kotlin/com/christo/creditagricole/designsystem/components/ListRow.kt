package com.christo.creditagricole.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.christo.creditagricole.designsystem.theme.ThemeDefaults

@Composable
fun ListRow(
    title: String,
    value: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    leadingBullet: Boolean = false,
    titleIndentDp: Int = 0,
    trailing: ListRowTrailing = ListRowTrailings.ChevronRight
) {
    val d = ThemeDefaults.dimens

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(d.itemCorner))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        tonalElevation = d.spacingNone,
        shadowElevation = d.spacingNone,
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = d.itemPadding, vertical = d.itemPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingBullet) {
                Box(
                    modifier = Modifier
                        .size(d.bulletSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                )
                Spacer(Modifier.width(d.spacing10))
            }

            if (titleIndentDp > 0) Spacer(Modifier.width(titleIndentDp.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Spacer(Modifier.width(d.spacing10))

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
            )

            Spacer(Modifier.width(d.spacing8))

            trailing.textGlyph?.let { glyph ->
                Text(
                    text = glyph,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                )
            }
        }
    }
}
