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
import com.christo.creditagricole.presentation.utils.formatAsCurrency
import creditagricole.composeapp.generated.resources.Res
import creditagricole.composeapp.generated.resources.account_detail_credit_label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlin.math.abs
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class AccountDetailViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `OnAppear loads operations and updates state`() = runTest {
        withEnvironment {
            val operationOne = operation(
                id = "op-1",
                amountMinor = -2_500,
                description = "Netflix",
                type = OperationType.DEBIT,
                epochMillis = 1_000L
            )
            val operationTwo = operation(
                id = "op-2",
                amountMinor = 5_000,
                description = "Salaire",
                type = OperationType.CREDIT,
                epochMillis = 2_000L
            )
            repository.enqueue(Result.success(listOf(operationOne, operationTwo)))

            viewModel.dispatch(AccountDetailIntent.OnAppear)
            advanceUntilIdle()

            assertEquals(1, repository.invocationCount)
            val state = viewModel.state.value
            assertFalse(state.isLoading)
            assertTrue(state.hasLoaded)
            assertNull(state.errorMessage)
            assertEquals(
                expectedUiItems(listOf(operationTwo, operationOne)),
                state.operations
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `OnAppear does not reload when operations already present`() = runTest {
        withEnvironment {
            repository.enqueue(Result.success(listOf(operation(id = "first"))))
            viewModel.dispatch(AccountDetailIntent.OnAppear)
            advanceUntilIdle()
            assertEquals(1, repository.invocationCount)

            viewModel.dispatch(AccountDetailIntent.OnAppear)
            advanceUntilIdle()

            assertEquals(1, repository.invocationCount)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `OnRetry refreshes operations`() = runTest {
        withEnvironment {
            repository.enqueue(Result.success(listOf(operation(id = "initial"))))
            viewModel.dispatch(AccountDetailIntent.OnAppear)
            advanceUntilIdle()
            assertEquals(1, repository.invocationCount)

            val refreshed = operation(id = "refresh", amountMinor = 1_234)
            repository.enqueue(Result.success(listOf(refreshed)))

            viewModel.dispatch(AccountDetailIntent.OnRetry)
            advanceUntilIdle()

            assertEquals(2, repository.invocationCount)
            val state = viewModel.state.value
            assertTrue(state.hasLoaded)
            assertEquals(expectedUiItems(listOf(refreshed)), state.operations)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Load failure propagates throwable message and effect`() = runTest {
        withEnvironment {
            repository.enqueue(Result.failure(IllegalStateException("Network down")))

            viewModel.dispatch(AccountDetailIntent.OnAppear)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertFalse(state.isLoading)
            assertTrue(state.hasLoaded)
            assertEquals("Network down", state.errorMessage)
            assertTrue(state.operations.isEmpty())
            assertEquals(
                listOf<AccountDetailEffect>(AccountDetailEffect.ShowError("Network down")),
                effects
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Load failure with blank message falls back to localized string`() = runTest {
        withEnvironment {
            val fallback = "Impossible de charger les operations."
            strings.fallbackMessage = fallback
            repository.enqueue(Result.failure(IllegalArgumentException("")))

            viewModel.dispatch(AccountDetailIntent.OnAppear)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals(fallback, state.errorMessage)
            assertEquals(
                listOf<AccountDetailEffect>(AccountDetailEffect.ShowError(fallback)),
                effects
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `OnBackClicked exposes navigation target then clears after consumption`() = runTest {
        withEnvironment {
            viewModel.dispatch(AccountDetailIntent.OnBackClicked)
            advanceUntilIdle()

            assertIs<AccountDetailNavigation.Back>(viewModel.state.value.navigationTarget)

            viewModel.dispatch(AccountDetailIntent.OnNavigationConsumed)
            advanceUntilIdle()

            assertNull(viewModel.state.value.navigationTarget)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun TestScope.withEnvironment(
        block: suspend TestEnvironment.() -> Unit
    ) {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val repository = FakeOperationRepository()
        val dispatcherProvider = testDispatcherProvider(dispatcher)
        val strings = FakeAccountDetailStrings()
        val useCase = GetOperationsForAccountUseCase(repository)
        val viewModel = AccountDetailViewModel(
            dispatcherProvider = dispatcherProvider,
            bankName = BANK_NAME,
            account = ACCOUNT,
            getOperationsForAccountUseCase = useCase,
            strings = strings
        )
        val collectedEffects = mutableListOf<AccountDetailEffect>()
        val effectJob = launch(dispatcher) {
            viewModel.effects.collect { collectedEffects += it }
        }
        val environment = TestEnvironment(
            dispatcher = dispatcher,
            repository = repository,
            viewModel = viewModel,
            effects = collectedEffects,
            effectJob = effectJob,
            strings = strings
        )
        try {
            block(environment)
        } finally {
            effectJob.cancel()
            viewModel.clear()
        }
    }

    private fun testDispatcherProvider(dispatcher: CoroutineDispatcher) =
        object : DispatcherProvider {
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
                    amount = operation.amount.formatAsCurrency(),
                    typeLabelRes = when (operation.type) {
                        OperationType.CREDIT -> Res.string.account_detail_credit_label
                        OperationType.DEBIT -> null
                    },
                    executedAt = formatDate(operation.executedAtEpochMillis)
                )
            }

    private fun formatDate(epochMillis: Long): String {
        if (epochMillis <= 0) return UNKNOWN_DATE
        return runCatching {
            val instant = Instant.fromEpochMilliseconds(epochMillis)
            val date = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
            val day = date.dayOfMonth.toString().padStart(2, '0')
            val month = date.monthNumber.toString().padStart(2, '0')
            val year = date.year.toString().padStart(4, '0')
            "$day/$month/$year"
        }.getOrElse { UNKNOWN_DATE }
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
        amount = Money(
            amountInMinor = when (type) {
                OperationType.DEBIT -> -abs(amountMinor)
                OperationType.CREDIT -> abs(amountMinor)
            },
            currencyCode = "EUR"
        ),
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
            val result = if (queue.isEmpty()) null else queue.removeFirst()
            assertNotNull(result) { "No result enqueued for getOperations" }
            return result.getOrThrow()
        }

        override suspend fun getOperation(operationId: OperationId) =
            error("Not used in tests")
    }

    private data class TestEnvironment(
        val dispatcher: CoroutineDispatcher,
        val repository: FakeOperationRepository,
        val viewModel: AccountDetailViewModel,
        val effects: MutableList<AccountDetailEffect>,
        val effectJob: Job,
        val strings: FakeAccountDetailStrings
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
        private const val UNKNOWN_DATE = "--/--/----"
    }

    private class FakeAccountDetailStrings : AccountDetailStrings {
        var fallbackMessage: String = "Unable to load operations"

        override suspend fun loadOperationsError(): String = fallbackMessage
    }
}
