package com.christo.creditagricole.data.exceptions

open class DataException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

class NetworkException(
    message: String,
    cause: Throwable? = null
) : DataException(message, cause)

class DataMappingException(
    message: String,
    cause: Throwable? = null
) : DataException(message, cause)
