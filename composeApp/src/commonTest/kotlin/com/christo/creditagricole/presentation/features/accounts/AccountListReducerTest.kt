package com.christo.creditagricole.presentation.features.accounts

import com.christo.creditagricole.domain.model.Account
import com.christo.creditagricole.domain.model.AccountId
import com.christo.creditagricole.domain.model.AccountKind
import com.christo.creditagricole.domain.model.Bank
import com.christo.creditagricole.domain.model.BankId
import com.christo.creditagricole.domain.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AccountListReducerTest {

    private val bankId = BankId("bank-1")
    private val otherBankId = BankId("bank-2")

    @Test
    fun `Loading clears error and navigation`() {
        val initial = AccountListState(
            isLoading = false,
            errorMessage = "Oops",
            navigationTarget = AccountListNavigation.ToAccountDetail("Bank", account())
        )

        val updated = AccountListReducer.reduce(initial, AccountListResult.Loading)

        assertTrue(updated.isLoading)
        assertNull(updated.errorMessage)
        assertNull(updated.navigationTarget)
    }

    @Test
    fun `BanksContent updates sections and loaded flag`() {
        val sections = listOf(bankSection(bankId))
        val initial = AccountListState(hasLoaded = false)

        val updated = AccountListReducer.reduce(
            initial,
            AccountListResult.BanksContent(sections = sections, markLoaded = true)
        )

        assertFalse(updated.isLoading)
        assertTrue(updated.hasLoaded)
        assertEquals(sections, updated.sections)
    }

    @Test
    fun `ToggleExpanded flips expanded flag and clears account error`() {
        val section = bankSection(
            bankId = bankId,
            accountCell = accountCell(
                bankId = bankId,
                isExpanded = false,
                accountsError = "Error"
            )
        )
        val initial = state(section)

        val updated = AccountListReducer.reduce(
            initial,
            AccountListResult.ToggleExpanded(bankId)
        )

        val updatedCell = updated.sections.single().banks.single()
        assertTrue(updatedCell.isExpanded)
        assertNull(updatedCell.accountsError)
    }

    @Test
    fun `AccountsLoading marks cell loading and expanded`() {
        val section = bankSection(
            bankId = bankId,
            accountCell = accountCell(bankId = bankId, isExpanded = false)
        )
        val initial = state(section)

        val updated = AccountListReducer.reduce(
            initial,
            AccountListResult.AccountsLoading(bankId)
        )

        val updatedCell = updated.sections.single().banks.single()
        assertTrue(updatedCell.isExpanded)
        assertTrue(updatedCell.isLoadingAccounts)
        assertNull(updatedCell.accountsError)
    }

    @Test
    fun `AccountsContent populates accounts clears error and computes total`() {
        val existing = accountCell(bankId = bankId, totalBalance = Money(500, "EUR"))
        val sections = listOf(
            bankSection(bankId = bankId, accountCell = existing),
            bankSection(bankId = otherBankId)
        )
        val initial = state(sections)
        val accounts = listOf(
            accountItem("acc-1", amountMinor = 1500),
            accountItem("acc-2", amountMinor = -500)
        )

        val updated = AccountListReducer.reduce(
            initial,
            AccountListResult.AccountsContent(bankId = bankId, accounts = accounts)
        )

        val updatedCell = updated.sections.first { it.banks.first().id == bankId }.banks.first()
        assertFalse(updatedCell.isLoadingAccounts)
        assertEquals(accounts, updatedCell.accounts)
        assertNull(updatedCell.accountsError)
        assertEquals(
            Money(amountInMinor = 1000, currencyCode = "EUR", currencySymbolOverride = "€"),
            updatedCell.totalBalance
        )
        val untouchedCell = updated.sections.first { it.banks.first().id == otherBankId }.banks.first()
        assertEquals(Money(500, "EUR"), untouchedCell.totalBalance)
    }

    @Test
    fun `AccountsError clears loading and stores message`() {
        val section = bankSection(
            bankId = bankId,
            accountCell = accountCell(bankId = bankId, isLoadingAccounts = true)
        )
        val initial = state(section)

        val updated = AccountListReducer.reduce(
            initial,
            AccountListResult.AccountsError(bankId = bankId, message = "Boom")
        )

        val updatedCell = updated.sections.single().banks.single()
        assertFalse(updatedCell.isLoadingAccounts)
        assertEquals("Boom", updatedCell.accountsError)
    }

    @Test
    fun `NavigateToAccountDetail sets navigation target`() {
        val initial = AccountListState()
        val account = account()

        val updated = AccountListReducer.reduce(
            initial,
            AccountListResult.NavigateToAccountDetail(bankName = "Bank", account = account)
        )

        assertEquals(
            AccountListNavigation.ToAccountDetail("Bank", account),
            updated.navigationTarget
        )
    }

    @Test
    fun `NavigationConsumed clears navigation`() {
        val initial = AccountListState(
            navigationTarget = AccountListNavigation.ToAccountDetail("Bank", account())
        )

        val updated = AccountListReducer.reduce(initial, AccountListResult.NavigationConsumed)

        assertNull(updated.navigationTarget)
    }

    private fun accountItem(
        id: String,
        amountMinor: Long = 0,
        currency: String = "EUR"
    ): AccountItemUi = AccountItemUi(
        id = AccountId(id),
        title = "Account $id",
        account = Account(
            id = AccountId(id),
            bankId = bankId,
            name = "Account $id",
            balance = Money(amountMinor, currency),
            kind = AccountKind.CHECKING
        )
    )

    private fun account(
        id: String = "account-1",
        bankId: BankId = BankId("bank-1")
    ): Account = Account(
        id = AccountId(id),
        bankId = bankId,
        name = "Compte",
        balance = Money(1000, "EUR"),
        kind = AccountKind.CHECKING
    )

    private fun bankSection(
        bankId: BankId,
        accountCell: AccountCellUi = accountCell(bankId),
        title: String = "Section ${bankId.value}"
    ): BankSectionUi = BankSectionUi(
        title = title,
        banks = listOf(accountCell)
    )

    private fun state(section: BankSectionUi) = AccountListState(sections = listOf(section))

    private fun state(sections: List<BankSectionUi>) = AccountListState(sections = sections)

    private fun accountCell(
        bankId: BankId,
        isExpanded: Boolean = false,
        isLoadingAccounts: Boolean = false,
        accountsError: String? = null,
        totalBalance: Money? = Money(500, "EUR")
    ): AccountCellUi = AccountCellUi(
        id = bankId,
        title = "Bank ${bankId.value}",
        isCreditAgricole = bankId.value.startsWith("bank-1"),
        isExpanded = isExpanded,
        isLoadingAccounts = isLoadingAccounts,
        accounts = emptyList(),
        accountsError = accountsError,
        totalBalance = totalBalance
    )
}
