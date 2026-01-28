package com.christo.creditagricole.domain.usecase

import com.christo.creditagricole.domain.model.Account
import com.christo.creditagricole.domain.model.AccountId
import com.christo.creditagricole.domain.model.AccountKind
import com.christo.creditagricole.domain.model.Bank
import com.christo.creditagricole.domain.model.BankId
import com.christo.creditagricole.domain.model.Money
import com.christo.creditagricole.domain.model.Operation
import com.christo.creditagricole.domain.model.OperationId
import com.christo.creditagricole.domain.model.OperationType
import com.christo.creditagricole.domain.repository.AccountRepository
import com.christo.creditagricole.domain.repository.BankRepository
import com.christo.creditagricole.domain.repository.OperationRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest

class GetBanksUseCaseTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Banks are sorted alphabetically ignoring case`() = runTest {
        val repository = FakeBankRepository(
            banksResult = listOf(
                bank(id = "b", name = "zeta"),
                bank(id = "a", name = "Alpha"),
                bank(id = "c", name = "beta")
            )
        )
        val useCase = GetBanksUseCase(repository)

        val result = useCase()

        assertEquals(listOf("Alpha", "beta", "zeta"), result.map(Bank::name))
        assertEquals(1, repository.getBanksCalls)
    }
}

class GetMockBanksUseCaseTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Mock banks are sorted alphabetically ignoring case`() = runTest {
        val repository = FakeBankRepository(
            mockBanksResult = listOf(
                bank(id = "mock-2", name = "delta"),
                bank(id = "mock-1", name = "Charlie")
            )
        )
        val useCase = GetMockBanksUseCase(repository)

        val result = useCase()

        assertEquals(listOf("Charlie", "delta"), result.map(Bank::name))
        assertEquals(1, repository.getMockBanksCalls)
    }
}

class GetAccountsForBankUseCaseTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Accounts are sorted by kind then name`() = runTest {
        val bankId = BankId("bank-1")
        val repository = FakeAccountRepository(
            accountsByBank = mapOf(
                bankId to listOf(
                    account(id = "a3", name = "Zed", kind = AccountKind.CHECKING),
                    account(id = "a2", name = "Alpha", kind = AccountKind.SAVINGS),
                    account(id = "a1", name = "beta", kind = AccountKind.CHECKING)
                )
            )
        )
        val useCase = GetAccountsForBankUseCase(repository)

        val result = useCase(GetAccountsForBankUseCase.Params(bankId))

        assertEquals(
            listOf("beta", "Zed", "Alpha"),
            result.map(Account::name)
        )
        assertEquals(1, repository.getAccountsCalls[bankId])
    }
}

class GetOperationsForAccountUseCaseTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Operations are sorted by executed date descending`() = runTest {
        val accountId = AccountId("account-1")
        val repository = FakeOperationRepository(
            operationsByAccount = mapOf(
                accountId to listOf(
                    operation(id = "op-1", executedAt = 1_000L),
                    operation(id = "op-2", executedAt = 5_000L),
                    operation(id = "op-3", executedAt = 3_000L)
                )
            )
        )
        val useCase = GetOperationsForAccountUseCase(repository)

        val result = useCase(GetOperationsForAccountUseCase.Params(accountId))

        assertEquals(
            listOf("op-2", "op-3", "op-1"),
            result.map { it.id.value }
        )
        assertEquals(1, repository.getOperationsCalls[accountId])
        assertTrue(result.zipWithNext().all { it.first.executedAtEpochMillis >= it.second.executedAtEpochMillis })
    }
}

private class FakeBankRepository(
    private val banksResult: List<Bank> = emptyList(),
    private val mockBanksResult: List<Bank> = emptyList()
) : BankRepository {
    var getBanksCalls: Int = 0
        private set
    var getMockBanksCalls: Int = 0
        private set

    override suspend fun getBanks(): List<Bank> {
        getBanksCalls += 1
        return banksResult
    }

    override suspend fun getMockBanks(): List<Bank> {
        getMockBanksCalls += 1
        return mockBanksResult
    }
}

private class FakeAccountRepository(
    private val accountsByBank: Map<BankId, List<Account>>
) : AccountRepository {
    val getAccountsCalls: MutableMap<BankId, Int> = mutableMapOf()

    override suspend fun getAccounts(bankId: BankId): List<Account> {
        getAccountsCalls[bankId] = (getAccountsCalls[bankId] ?: 0) + 1
        return accountsByBank[bankId] ?: emptyList()
    }

    override suspend fun getAccount(accountId: AccountId): Account? = null
}

private class FakeOperationRepository(
    private val operationsByAccount: Map<AccountId, List<Operation>>
) : OperationRepository {
    val getOperationsCalls: MutableMap<AccountId, Int> = mutableMapOf()

    override suspend fun getOperations(accountId: AccountId): List<Operation> {
        getOperationsCalls[accountId] = (getOperationsCalls[accountId] ?: 0) + 1
        return operationsByAccount[accountId] ?: emptyList()
    }

    override suspend fun getOperation(operationId: OperationId): Operation? = null
}

private fun bank(id: String, name: String): Bank =
    Bank(
        id = BankId(id),
        name = name,
        countryCode = "FR",
        isCreditAgricole = id.startsWith("ca", ignoreCase = true)
    )

private fun account(
    id: String,
    name: String,
    kind: AccountKind,
    bankId: BankId = BankId("bank-1")
): Account = Account(
    id = AccountId(id),
    bankId = bankId,
    name = name,
    balance = Money(100, "EUR"),
    kind = kind
)

private fun operation(
    id: String,
    executedAt: Long,
    accountId: AccountId = AccountId("account-1")
): Operation = Operation(
    id = OperationId(id),
    accountId = accountId,
    description = "Op $id",
    amount = Money(
        amountInMinor = if (id.hashCode() % 2 == 0) 100 else -100,
        currencyCode = "EUR"
    ),
    type = if (id.hashCode() % 2 == 0) OperationType.CREDIT else OperationType.DEBIT,
    executedAtEpochMillis = executedAt
)
