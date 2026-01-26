package com.christo.creditagricole.designsystem.components

import androidx.compose.runtime.Immutable

@Immutable
data class ListRowTrailing(
    val textGlyph: String? = null // "›", "⌄", etc (ou icône si tu préfères)
)

object ListRowTrailings {
    val None = ListRowTrailing(null)
    val ChevronRight = ListRowTrailing("›")
    val ChevronDown = ListRowTrailing("⌄")
    val ChevronUp = ListRowTrailing("⌃")
}