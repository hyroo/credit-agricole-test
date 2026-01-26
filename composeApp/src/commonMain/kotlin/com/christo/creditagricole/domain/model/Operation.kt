package com.christo.creditagricole.domain.model

data class Operation(
    val id: OperationId,
    val accountId: AccountId,
    val description: String,
    val amount: Money,
    val type: OperationType,
    val executedAtEpochMillis: Long
) {
    init {
        require(description.isNotBlank()) { "Operation description cannot be blank." }
        when (type) {
            OperationType.CREDIT -> require(!amount.isNegative) {
                "Credit operations expect a positive monetary amount."
            }

            OperationType.DEBIT -> require(!amount.isPositive) {
                "Debit operations expect a negative monetary amount."
            }
        }
    }
}

enum class OperationType {
    CREDIT,
    DEBIT
}
