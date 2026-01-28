package com.christo.creditagricole.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class Colors(
    val background: Color,
    val surface: Color,
    val onBackground: Color,
    val onSurface: Color,

    val primary: Color,
    val onPrimary: Color,

    val divider: Color,
    val muted: Color
)