package com.christo.creditagricole.domain.model

import kotlin.math.abs

data class Money(
    val amountInMinor: Long,
    val currencyCode: String,
    private val currencySymbolOverride: String? = null
) {
    init {
        require(currencyCode.length == 3 && currencyCode.all { it.isLetter() }) {
            "Currency code must follow the ISO-4217 alpha-3 format."
        }
        require(currencyCode == currencyCode.uppercase()) {
            "Currency code must use upper-case letters."
        }
        currencySymbolOverride?.let {
            require(it.isNotBlank()) { "Currency symbol override must not be blank." }
        }
    }

    private val normalizedSymbolOverride = currencySymbolOverride?.trim()?.takeIf { it.isNotEmpty() }

    val currencySymbol: String?
        get() = normalizedSymbolOverride ?: currencySymbolMap[currencyCode]

    val displayCurrency: String
        get() = currencySymbol ?: currencyCode

    val isPositive: Boolean get() = amountInMinor > 0
    val isNegative: Boolean get() = amountInMinor < 0
    val isZero: Boolean get() = amountInMinor == 0L

    fun negate(): Money = copy(amountInMinor = -amountInMinor)

    fun absolute(): Money = copy(amountInMinor = abs(amountInMinor))

    companion object {
        private val currencySymbolMap = mapOf(
            "EUR" to "€",
            "USD" to "$"
        )
    }
}
