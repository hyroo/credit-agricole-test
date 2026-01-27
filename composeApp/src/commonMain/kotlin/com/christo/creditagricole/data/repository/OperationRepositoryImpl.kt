package com.christo.creditagricole.data.repository

import com.christo.creditagricole.data.api.BankingApi
import com.christo.creditagricole.data.mapper.toDomain
import com.christo.creditagricole.domain.model.AccountId
import com.christo.creditagricole.domain.model.Operation
import com.christo.creditagricole.domain.model.OperationId
import com.christo.creditagricole.domain.repository.OperationRepository

class OperationRepositoryImpl(
    private val api: BankingApi
) : OperationRepository {

    override suspend fun getOperations(accountId: AccountId): List<Operation> =
        runCatching {
            executeCall("Impossible de recuperer les operations pour ${accountId.value}") {
                api.getOperationsForAccount(accountId.value).operations.map { it.toDomain(accountId) }
            }
        }.fold(
            onSuccess = { operations -> operations },
            onFailure = { throw it }
        )

    override suspend fun getOperation(operationId: OperationId): Operation? =
        executeCall("Impossible de recuperer l'operation ${operationId.value}") {
            api.getOperation(operationId.value).toDomain()
        }
}