package com.christo.creditagricole.data.repository

import com.christo.creditagricole.data.api.BankingApi
import com.christo.creditagricole.data.mapper.toDomain
import com.christo.creditagricole.domain.model.AccountId
import com.christo.creditagricole.domain.model.Operation
import com.christo.creditagricole.domain.model.OperationId
import com.christo.creditagricole.domain.repository.OperationRepository
import creditagricole.composeapp.generated.resources.Res
import creditagricole.composeapp.generated.resources.data_fetch_operation_error
import creditagricole.composeapp.generated.resources.data_fetch_operations_error
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.getString

class OperationRepositoryImpl(
    private val api: BankingApi
) : OperationRepository {

    override suspend fun getOperations(accountId: AccountId): List<Operation> =
        runCatching {
            executeCall(getString(Res.string.data_fetch_operations_error, accountId.value)) {
                api.getOperationsForAccount(accountId.value).operations.map { it.toDomain(accountId) }
            }
        }.fold(
            onSuccess = { operations -> operations },
            onFailure = { throw it }
        )

    override suspend fun getOperation(operationId: OperationId): Operation? =
        executeCall(getString(Res.string.data_fetch_operation_error, operationId.value)) {
            api.getOperation(operationId.value).toDomain()
        }
}
