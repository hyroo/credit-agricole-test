package com.christo.creditagricole.domain.repository

import com.christo.creditagricole.domain.model.Bank
import com.christo.creditagricole.domain.model.Account
import com.christo.creditagricole.domain.model.AccountId
import com.christo.creditagricole.domain.model.BankId
import com.christo.creditagricole.domain.model.Operation
import com.christo.creditagricole.domain.model.OperationId

interface BankRepository {
    suspend fun getBanks(): List<Bank>
    suspend fun getMockBanks(): List<Bank>
}

interface AccountRepository {
    suspend fun getAccounts(bankId: BankId): List<Account>
    suspend fun getAccount(accountId: AccountId): Account?
}

interface OperationRepository {
    suspend fun getOperations(accountId: AccountId): List<Operation>
    suspend fun getOperation(operationId: OperationId): Operation?
}
