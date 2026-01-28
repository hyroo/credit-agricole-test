package com.christo.creditagricole.designsystem.theme

import androidx.compose.ui.unit.dp

/**
 * Token exports for the dimension scale used across the design system.
 */
object DimensScheme {

    val Default: Dimens = run {
        val spacingNone = 0.dp
        val spacing2 = 2.dp
        val spacing4 = 4.dp
        val spacing6 = 6.dp
        val spacing8 = 8.dp
        val spacing10 = 10.dp
        val spacing12 = 12.dp
        val spacing14 = 14.dp
        val spacing16 = 16.dp
        val spacing20 = 20.dp
        val spacing24 = 24.dp

        Dimens(
            spacingNone = spacingNone,
            spacing2 = spacing2,
            spacing4 = spacing4,
            spacing6 = spacing6,
            spacing8 = spacing8,
            spacing10 = spacing10,
            spacing12 = spacing12,
            spacing14 = spacing14,
            spacing16 = spacing16,
            spacing20 = spacing20,
            spacing24 = spacing24,
            itemSpacing = spacing8,
            itemPadding = spacing12,
            itemCorner = spacing12,
            sectionSpacing = spacing14,
            screenPadding = spacing16,
            bulletSize = spacing10,
            strokeThin = spacing2,
        )
    }
}
