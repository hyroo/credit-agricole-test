package com.christo.creditagricole.domain.model

import kotlin.jvm.JvmInline

@JvmInline
value class BankId(val value: String) {
    init {
        require(value.isNotBlank()) { "Bank id cannot be blank." }
    }

    override fun toString(): String = value
}

@JvmInline
value class AccountId(val value: String) {
    init {
        require(value.isNotBlank()) { "Account id cannot be blank." }
    }

    override fun toString(): String = value
}

@JvmInline
value class OperationId(val value: String) {
    init {
        require(value.isNotBlank()) { "Operation id cannot be blank." }
    }

    override fun toString(): String = value
}
