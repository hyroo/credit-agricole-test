package com.christo.creditagricole.presentation.features.accounts

import com.christo.creditagricole.core.DispatcherProvider
import com.christo.creditagricole.domain.model.Account
import com.christo.creditagricole.domain.model.AccountId
import com.christo.creditagricole.domain.model.AccountKind
import com.christo.creditagricole.domain.model.Bank
import com.christo.creditagricole.domain.model.BankId
import com.christo.creditagricole.domain.model.Money
import com.christo.creditagricole.domain.repository.AccountRepository
import com.christo.creditagricole.domain.repository.BankRepository
import com.christo.creditagricole.domain.usecase.GetAccountsForBankUseCase
import com.christo.creditagricole.domain.usecase.GetBanksUseCase
import com.christo.creditagricole.domain.usecase.GetMockBanksUseCase
import com.christo.creditagricole.presentation.features.accounts.AccountListNavigation.ToAccountDetail
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.math.abs
import kotlin.test.assertFalse

class AccountListViewModelTest {

    @Test
    fun `OnAppear loads remote banks and sections`() = runTest {
        withEnvironment {
            val caBank = bank(id = "ca-1", name = "CA Languedoc", isCa = true)
            val otherBank = bank(id = "other-1", name = "Banque Populaire", isCa = false)

            accountsRepository.enqueue(caBank.id, Result.failure(RuntimeException("prefetch ignore")))
            accountsRepository.enqueue(otherBank.id, Result.failure(RuntimeException("prefetch ignore")))
            bankRepository.enqueueMock(Result.success(emptyList()))
            bankRepository.enqueueRemote(Result.success(listOf(otherBank, caBank)))

            viewModel.dispatch(AccountListIntent.OnAppear)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertTrue(state.hasLoaded)
            assertFalse(state.isLoading)
            assertNull(state.errorMessage)
            assertEquals(2, state.sections.size)
            assertEquals(strings.sectionCreditAgricoleTitle, state.sections[0].title)
            assertEquals(strings.sectionOthersTitle, state.sections[1].title)
            assertEquals(listOf(caBank.id), state.sections[0].banks.map { it.id })
            assertEquals(listOf(otherBank.id), state.sections[1].banks.map { it.id })
        }
    }

    @Test
    fun `Remote failure falls back to mock banks`() = runTest {
        withEnvironment {
            val fallbackBank = bank(id = "ca-fallback", name = "CA Sud", isCa = true)

            accountsRepository.enqueue(fallbackBank.id, Result.failure(RuntimeException("prefetch ignore")))
            bankRepository.enqueueMock(Result.success(listOf(fallbackBank)))
            bankRepository.enqueueRemote(Result.failure(IllegalStateException("network down")))

            viewModel.dispatch(AccountListIntent.OnAppear)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertTrue(state.hasLoaded)
            assertNull(state.errorMessage)
            assertEquals(1, state.sections.size)
            assertEquals(strings.sectionCreditAgricoleTitle, state.sections.first().title)
            assertEquals(listOf(fallbackBank.id), state.sections.first().banks.map { it.id })
            assertTrue(effects.isEmpty())
            assertEquals(1, bankRepository.remoteCalls)
            assertEquals(1, bankRepository.mockCalls)
        }
    }

    @Test
    fun `Remote failure without fallback exposes error`() = runTest {
        withEnvironment {
            strings.genericErrorMessage = "Unable to load banks"
            bankRepository.enqueueMock(Result.success(emptyList()))
            bankRepository.enqueueRemote(Result.failure(IllegalStateException("")))

            viewModel.dispatch(AccountListIntent.OnAppear)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals("Unable to load banks", state.errorMessage)
            assertEquals(
                listOf<BankListEffect>(BankListEffect.ShowError("Unable to load banks")),
                effects
            )
        }
    }

    @Test
    fun `OnBankToggled loads accounts when collapsed`() = runTest {
        withEnvironment {
            val bank = bank(id = "ca-1", name = "CA Bretagne", isCa = true)
            val accountOne = account(id = "acc-1", bankId = bank.id, name = "Compte Courant", amountMinor = 5_000)
            val accountTwo = account(id = "acc-2", bankId = bank.id, name = "Livret A", amountMinor = 2_000)

            accountsRepository.enqueue(bank.id, Result.failure(RuntimeException("prefetch ignore")))
            bankRepository.enqueueMock(Result.success(emptyList()))
            bankRepository.enqueueRemote(Result.success(listOf(bank)))

            viewModel.dispatch(AccountListIntent.OnAppear)
            advanceUntilIdle()

            accountsRepository.enqueue(bank.id, Result.success(listOf(accountOne, accountTwo)))

            viewModel.dispatch(AccountListIntent.OnBankToggled(bank.id))
            advanceUntilIdle()

            val section = viewModel.state.value.sections.first()
            val cell = section.banks.first { it.id == bank.id }
            assertTrue(cell.isExpanded)
            assertFalse(cell.isLoadingAccounts)
            assertEquals(listOf(accountOne.id, accountTwo.id), cell.accounts.map { it.id })
            assertNotNull(cell.totalBalance)
            assertEquals(accountOne.balance.amountInMinor + accountTwo.balance.amountInMinor, cell.totalBalance?.amountInMinor)
        }
    }

    @Test
    fun `OnAccountSelected exposes navigation target`() = runTest {
        withEnvironment {
            val account = account(id = "acc-10", bankId = BankId("bank-1"), name = "Compte Epargne", amountMinor = 1_000)
            val item = AccountItemUi(id = account.id, title = account.name, account = account)

            viewModel.dispatch(AccountListIntent.OnAccountSelected(account.bankId, "Bank Name", item))
            advanceUntilIdle()

            val nav = viewModel.state.value.navigationTarget
            assertIs<ToAccountDetail>(nav)
            assertEquals("Bank Name", nav.bankName)
            assertEquals(account, nav.account)

            viewModel.dispatch(AccountListIntent.OnNavigationConsumed)
            advanceUntilIdle()

            assertNull(viewModel.state.value.navigationTarget)
        }
    }

    private suspend fun TestScope.withEnvironment(
        block: suspend TestEnvironment.() -> Unit
    ) {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val bankRepository = FakeBankRepository()
        val accountRepository = FakeAccountRepository()
        val dispatcherProvider = testDispatcherProvider(dispatcher)
        val strings = FakeAccountListStrings()
        val viewModel = AccountListViewModel(
            dispatcherProvider = dispatcherProvider,
            getBanksUseCase = GetBanksUseCase(bankRepository),
            getMockBanksUseCase = GetMockBanksUseCase(bankRepository),
            getAccountsForBankUseCase = GetAccountsForBankUseCase(accountRepository),
            strings = strings
        )
        val effects = mutableListOf<BankListEffect>()
        val effectJob = launch(dispatcher) {
            viewModel.effects.collect { effects += it }
        }
        val environment = TestEnvironment(
            dispatcher = dispatcher,
            bankRepository = bankRepository,
            accountsRepository = accountRepository,
            viewModel = viewModel,
            effects = effects,
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

    private fun bank(
        id: String,
        name: String,
        isCa: Boolean,
        countryCode: String = "FR"
    ): Bank = Bank(
        id = BankId(id),
        name = name,
        countryCode = countryCode,
        isCreditAgricole = isCa
    )

    private fun account(
        id: String,
        bankId: BankId,
        name: String,
        amountMinor: Long,
        kind: AccountKind = AccountKind.CHECKING
    ): Account = Account(
        id = AccountId(id),
        bankId = bankId,
        name = name,
        balance = Money(amountMinor, "EUR"),
        kind = kind
    )

    private class FakeAccountListStrings : AccountListStrings {
        var emptyBanksMessage: String = "Aucune banque"
        var genericErrorMessage: String = "Erreur inconnue"
        var sectionCreditAgricoleTitle: String = "Banques Crédit Agricole"
        var sectionOthersTitle: String = "Autres banques"
        var loadAccountsErrorMessage: String = "Impossible de charger les comptes"

        override suspend fun emptyBanks(): String = emptyBanksMessage
        override suspend fun genericError(): String = genericErrorMessage
        override suspend fun sectionCreditAgricole(): String = sectionCreditAgricoleTitle
        override suspend fun sectionOthers(): String = sectionOthersTitle
        override suspend fun loadAccountsError(): String = loadAccountsErrorMessage
    }

    private class FakeBankRepository : BankRepository {
        private val remoteQueue = ArrayDeque<Result<List<Bank>>>()
        private val mockQueue = ArrayDeque<Result<List<Bank>>>()
        var remoteCalls: Int = 0
            private set
        var mockCalls: Int = 0
            private set

        fun enqueueRemote(result: Result<List<Bank>>) {
            remoteQueue.addLast(result)
        }

        fun enqueueMock(result: Result<List<Bank>>) {
            mockQueue.addLast(result)
        }

        override suspend fun getBanks(): List<Bank> {
            remoteCalls++
            val result = remoteQueue.removeFirstOrNull()
            assertNotNull(result) { "No remote banks result enqueued" }
            return result.getOrThrow()
        }

        override suspend fun getMockBanks(): List<Bank> {
            mockCalls++
            val result = mockQueue.removeFirstOrNull()
            assertNotNull(result) { "No mock banks result enqueued" }
            return result.getOrThrow()
        }
    }

    private class FakeAccountRepository : AccountRepository {
        private val queue = ArrayDeque<Pair<BankId, Result<List<Account>>>>()
        val calls = mutableListOf<BankId>()

        fun enqueue(bankId: BankId, result: Result<List<Account>>) {
            queue.addLast(bankId to result)
        }

        override suspend fun getAccounts(bankId: BankId): List<Account> {
            val entry = queue.removeFirstOrNull()
            assertNotNull(entry) { "No accounts result enqueued for $bankId" }
            val (expectedId, result) = entry
            assertEquals(expectedId, bankId)
            calls += bankId
            return result.getOrThrow()
        }

        override suspend fun getAccount(accountId: AccountId): Account? =
            error("Not used in tests")
    }

    private data class TestEnvironment(
        val dispatcher: CoroutineDispatcher,
        val bankRepository: FakeBankRepository,
        val accountsRepository: FakeAccountRepository,
        val viewModel: AccountListViewModel,
        val effects: MutableList<BankListEffect>,
        val effectJob: Job,
        val strings: FakeAccountListStrings
    )
}
