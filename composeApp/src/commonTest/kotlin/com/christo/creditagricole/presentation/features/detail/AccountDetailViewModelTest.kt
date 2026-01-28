package com.christo.creditagricole.presentation.features.detail

import com.christo.creditagricole.core.DispatcherProvider
import com.christo.creditagricole.domain.model.Account
import com.christo.creditagricole.domain.model.AccountId
import com.christo.creditagricole.domain.model.AccountKind
import com.christo.creditagricole.domain.model.BankId
import com.christo.creditagricole.domain.model.Money
import com.christo.creditagricole.domain.model.Operation
import com.christo.creditagricole.domain.model.OperationId
import com.christo.creditagricole.domain.model.OperationType
import com.christo.creditagricole.domain.repository.OperationRepository
import com.christo.creditagricole.domain.usecase.GetOperationsForAccountUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AccountDetailViewModelTest {

    @Test
    fun `initial state reflects provided account information`() = runTest {
        withViewModel {
            val state = viewModel.state.value

            assertEquals(BANK_NAME, state.bankName)
            assertEquals(ACCOUNT.name, state.accountName)
            assertEquals("Checking", state.accountKind)
            assertEquals(formatMoney(ACCOUNT.balance), state.balance)
            assertTrue(state.operations.isEmpty())
            assertFalse(state.hasLoaded)
            assertFalse(state.isLoading)
            assertNull(state.errorMessage)
        }
    }

    @Test
    fun `OnAppear loads operations and updates state`() = runTest {
        withViewModel {
            val operations = listOf(
                operation(
                    id = "op-1",
                    amountMinor = -12_34,
                    type = OperationType.DEBIT,
                    description = "Zeta",
                    epochMillis = 1_000L
                ),
                operation(
                    id = "op-2",
                    amountMinor = 45_67,
                    type = OperationType.CREDIT,
                    description = "Alpha",
                    epochMillis = 2_000L
                ),
                operation(
                    id = "op-3",
                    amountMinor = -89_01,
                    type = OperationType.DEBIT,
                    description = "beta",
                    epochMillis = 2_000L
                )
            )
            repository.enqueue(Result.success(operations))
            val observedStates = mutableListOf<AccountDetailState>()
            val stateJob = launch(dispatcher) {
                viewModel.state.drop(1).take(2).collect { observedStates += it }
            }

            viewModel.dispatch(AccountDetailIntent.OnAppear)
            advanceUntilIdle()
            stateJob.join()

            assertEquals(1, repository.invocationCount)

            val loadingState = observedStates.first()
            assertTrue(loadingState.isLoading)
            assertNull(loadingState.errorMessage)

            val finalState = viewModel.state.value
            assertFalse(finalState.isLoading)
            assertNull(finalState.errorMessage)
            assertTrue(finalState.hasLoaded)
            assertEquals(expectedUiItems(operations), finalState.operations)
            assertTrue(effects.isEmpty())
        }
    }

    @Test
    fun `OnAppear does not reload when operations already present`() = runTest {
        withViewModel {
            repository.enqueue(Result.success(listOf(operation(id = "initial"))))

            viewModel.dispatch(AccountDetailIntent.OnAppear)
            advanceUntilIdle()

            viewModel.dispatch(AccountDetailIntent.OnAppear)
            advanceUntilIdle()

            assertEquals(1, repository.invocationCount)
        }
    }

    @Test
    fun `OnRetry reloads operations`() = runTest {
        withViewModel {
            repository.enqueue(Result.success(listOf(operation(id = "initial"))))

            viewModel.dispatch(AccountDetailIntent.OnAppear)
            advanceUntilIdle()

            repository.enqueue(Result.success(listOf(operation(id = "retry"))))

            viewModel.dispatch(AccountDetailIntent.OnRetry)
            advanceUntilIdle()

            assertEquals(2, repository.invocationCount)
            val ids = viewModel.state.value.operations.map { it.id.value }
            assertTrue("retry" in ids)
        }
    }

    @Test
    fun `OnBackClicked exposes navigation target and OnNavigationConsumed clears it`() = runTest {
        withViewModel {
            viewModel.dispatch(AccountDetailIntent.OnBackClicked)
            advanceUntilIdle()

            assertIs<AccountDetailNavigation.Back>(viewModel.state.value.navigationTarget)

            viewModel.dispatch(AccountDetailIntent.OnNavigationConsumed)
            advanceUntilIdle()

            assertNull(viewModel.state.value.navigationTarget)
        }
    }

    @Test
    fun `failure when loading operations exposes error state and effect`() = runTest {
        withViewModel {
            repository.enqueue(Result.failure(IllegalStateException("Network down")))

            viewModel.dispatch(AccountDetailIntent.OnAppear)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertFalse(state.isLoading)
            assertTrue(state.hasLoaded)
            assertEquals("Network down", state.errorMessage)
            assertTrue(state.operations.isEmpty())
            assertEquals(
                listOf(AccountDetailEffect.ShowError("Network down")),
                effects
            )
        }
    }

    @Test
    fun `failure with empty error message falls back to default`() = runTest {
        withViewModel {
            repository.enqueue(Result.failure(IllegalArgumentException("")))

            viewModel.dispatch(AccountDetailIntent.OnAppear)
            advanceUntilIdle()

            val expectedMessage = "Impossible de charger les operations."
            val state = viewModel.state.value
            assertEquals(expectedMessage, state.errorMessage)
            assertEquals(
                listOf(AccountDetailEffect.ShowError(expectedMessage)),
                effects
            )
        }
    }

    private suspend fun TestScope.withViewModel(block: suspend TestEnvironment.() -> Unit) {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val dispatcherProvider = testDispatcherProvider(dispatcher)
        val repository = FakeOperationRepository()
        val useCase = GetOperationsForAccountUseCase(repository)
        val viewModel = AccountDetailViewModel(
            dispatcherProvider = dispatcherProvider,
            bankName = BANK_NAME,
            account = ACCOUNT,
            getOperationsForAccountUseCase = useCase
        )
        val effects = mutableListOf<AccountDetailEffect>()
        val effectJob = launch(dispatcher) {
            viewModel.effects.collect { effect -> effects += effect }
        }
        try {
            block(
                TestEnvironment(
                    dispatcher = dispatcher,
                    repository = repository,
                    viewModel = viewModel,
                    effects = effects
                )
            )
        } finally {
            viewModel.clear()
            effectJob.cancel()
        }
    }

    private fun testDispatcherProvider(dispatcher: CoroutineDispatcher) = object : DispatcherProvider {
        override val main: CoroutineDispatcher = dispatcher
        override val io: CoroutineDispatcher = dispatcher
        override val computation: CoroutineDispatcher = dispatcher
        override val unconfined: CoroutineDispatcher = dispatcher
    }

    private fun expectedUiItems(operations: List<Operation>): List<OperationItemUi> =
        operations
            .sortedWith(
                compareByDescending<Operation> { it.executedAtEpochMillis }
                    .thenBy { it.description.lowercase() }
            )
            .map { operation ->
                OperationItemUi(
                    id = operation.id,
                    description = operation.description,
                    amount = formatMoney(operation.amount),
                    typeLabel = when (operation.type) {
                        OperationType.CREDIT -> "Crédit"
                        OperationType.DEBIT -> ""
                    },
                    executedAt = formatDate(operation.executedAtEpochMillis)
                )
            }

    private fun formatMoney(money: Money): String {
        val absolute = abs(money.amountInMinor)
        val units = absolute / 100
        val cents = absolute % 100
        val centsString = cents.toString().padStart(2, '0')
        val sign = if (money.amountInMinor < 0) "-" else ""
        return "$sign$units.$centsString ${money.currencyCode}"
    }

    private fun formatDate(epochMillis: Long): String {
        if (epochMillis <= 0) return "--/--/----"
        return runCatching {
            val instant = Instant.fromEpochMilliseconds(epochMillis)
            val date = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
            val day = date.dayOfMonth.toString().padStart(2, '0')
            val month = date.monthNumber.toString().padStart(2, '0')
            val year = date.year.toString().padStart(4, '0')
            "$day/$month/$year"
        }.getOrElse { "--/--/----" }
    }

    private fun operation(
        id: String = "op-1",
        amountMinor: Long = -1_00,
        type: OperationType = OperationType.DEBIT,
        description: String = "Operation",
        epochMillis: Long = 1_000L
    ): Operation = Operation(
        id = OperationId(id),
        accountId = ACCOUNT.id,
        description = description,
        amount = Money(amountMinor, "EUR"),
        type = type,
        executedAtEpochMillis = epochMillis
    )

    private class FakeOperationRepository : OperationRepository {
        private val queue = ArrayDeque<Result<List<Operation>>>()
        var invocationCount: Int = 0
            private set

        fun enqueue(result: Result<List<Operation>>) {
            queue.addLast(result)
        }

        override suspend fun getOperations(accountId: AccountId): List<Operation> {
            invocationCount++
            val result = if (queue.isNotEmpty()) queue.removeFirst() else error("No result enqueued")
            return result.getOrThrow()
        }

        override suspend fun getOperation(operationId: OperationId) = error("Not used in tests")
    }

    private data class TestEnvironment(
        val dispatcher: StandardTestDispatcher,
        val repository: FakeOperationRepository,
        val viewModel: AccountDetailViewModel,
        val effects: MutableList<AccountDetailEffect>
    )

    companion object {
        private const val BANK_NAME = "Credit Agricole"
        private val ACCOUNT = Account(
            id = AccountId("account-1"),
            bankId = BankId("bank-1"),
            name = "Compte Courant",
            balance = Money(12_345, "EUR"),
            kind = AccountKind.CHECKING
        )
    }
}
