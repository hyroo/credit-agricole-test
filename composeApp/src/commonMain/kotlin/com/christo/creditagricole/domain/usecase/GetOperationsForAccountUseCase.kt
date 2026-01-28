package com.christo.creditagricole.domain.usecase

import com.christo.creditagricole.domain.SuspendUseCase
import com.christo.creditagricole.domain.model.AccountId
import com.christo.creditagricole.domain.model.Operation
import com.christo.creditagricole.domain.repository.OperationRepository


class GetOperationsForAccountUseCase(
    private val operationRepository: OperationRepository
) : SuspendUseCase<GetOperationsForAccountUseCase.Params, List<Operation>> {

    data class Params(val accountId: AccountId)

    override suspend fun invoke(params: Params): List<Operation> {
        return operationRepository
            .getOperations(params.accountId)
            .sortedByDescending { it.executedAtEpochMillis }
    }
}
