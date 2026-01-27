package com.christo.creditagricole.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme as M3ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalColors = staticCompositionLocalOf<Colors> {
    error("LocalColors not provided. Wrap your UI with Theme().")
}

val LocalDimens = staticCompositionLocalOf { Dimens() } // ou DsDimens si ton nom

@Composable
fun Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colors: Colors = if (darkTheme) ColorScheme.Dark else ColorScheme.Light,
    dimens: Dimens = Dimens(),
    content: @Composable () -> Unit
) {
    val m3 = colors.toMaterial3(darkTheme)

    CompositionLocalProvider(
        LocalColors provides colors,
        LocalDimens provides dimens
    ) {
        MaterialTheme(
            colorScheme = m3,
            content = content
        )
    }
}

object ThemeDefaults {
    val colors: Colors
        @Composable get() = LocalColors.current

    val dimens: Dimens
        @Composable get() = LocalDimens.current
}

private fun Colors.toMaterial3(darkTheme: Boolean): M3ColorScheme =
    if (darkTheme) {
        darkColorScheme(
            background = background,
            surface = surface,
            onBackground = onBackground,
            onSurface = onSurface,
            primary = primary,
            onPrimary = onPrimary
        )
    } else {
        lightColorScheme(
            background = background,
            surface = surface,
            onBackground = onBackground,
            onSurface = onSurface,
            primary = primary,
            onPrimary = onPrimary
        )
    }
