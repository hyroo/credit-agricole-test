package com.christo.creditagricole.presentation.utils

import com.christo.creditagricole.domain.model.Money
import kotlin.math.absoluteValue

fun Money.formatAsCurrency(): String {
    val absMinor = amountInMinor.absoluteValue
    val major = absMinor / 100
    val minor = (absMinor % 100).toInt()
    val amount = buildString {
        append(major)
        append('.')
        append(minor.toString().padStart(2, '0'))
        append(' ')
        append(displayCurrency)
    }
    return if (amountInMinor < 0) "-$amount" else amount
}
