package com.christo.creditagricole.data.repository

import com.christo.creditagricole.data.exceptions.DataException
import com.christo.creditagricole.data.exceptions.NetworkException

internal inline fun <T> executeCall(
    errorMessage: String,
    block: () -> T
): T = try {
    block()
} catch (exception: DataException) {
    throw exception
} catch (throwable: Throwable) {
    throw NetworkException(errorMessage, throwable)
}
