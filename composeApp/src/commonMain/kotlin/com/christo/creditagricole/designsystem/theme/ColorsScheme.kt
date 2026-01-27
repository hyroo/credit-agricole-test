package com.christo.creditagricole.designsystem.theme

import androidx.compose.ui.graphics.Color

object ColorScheme {

    val Light = Colors(
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFFFFFFF),
        onBackground = Color(0xFF0B0B0B),
        onSurface = Color(0xFF1C1C1C),

        primary = Color(0xFF0A8F55),
        onPrimary = Color.White,

        divider = Color(0xFFE6E6E6),
        muted = Color(0xFF9E9E9E)
    )

    val Dark = Colors(
        background = Color(0xFF0B0B0B),
        surface = Color(0xFF0B0B0B),
        onBackground = Color(0xFFF2F2F2),
        onSurface = Color(0xFFE0E0E0),

        primary = Color(0xFF32C07B),
        onPrimary = Color.Black,

        divider = Color(0xFF2A2A2A),
        muted = Color(0xFF8A8A8A)
    )
}