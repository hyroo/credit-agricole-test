package com.christo.creditagricole.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MoneyTest {

    @Test
    fun `Symbol override takes precedence and is trimmed`() {
        val money = Money(
            amountInMinor = 1234,
            currencyCode = "USD",
            currencySymbolOverride = " $ "
        )

        assertEquals("$", money.currencySymbol)
        assertEquals("$", money.displayCurrency)
    }

    @Test
    fun `Known currency falls back to symbol map`() {
        val money = Money(amountInMinor = 100, currencyCode = "EUR")

        assertEquals("€", money.currencySymbol)
        assertEquals("€", money.displayCurrency)
    }

    @Test
    fun `Lowercase currency code is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            Money(amountInMinor = 100, currencyCode = "eur")
        }
    }

    @Test
    fun `Negate toggles sign while absolute returns positive amount`() {
        val original = Money(amountInMinor = -2500, currencyCode = "EUR")

        val negated = original.negate()
        assertTrue(negated.isPositive)
        assertEquals(2500, negated.amountInMinor)

        val absolute = original.absolute()
        assertTrue(absolute.isPositive)
        assertEquals(2500, absolute.amountInMinor)
    }
}

class BankTest {

    @Test
    fun `Valid bank keeps provided attributes`() {
        val bank = Bank(
            id = BankId("bank-1"),
            name = "Crédit Agricole",
            countryCode = "FR",
            isCreditAgricole = true
        )

        assertEquals("Crédit Agricole", bank.name)
        assertTrue(bank.isCreditAgricole)
    }

    @Test
    fun `Blank name is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            Bank(
                id = BankId("bank-1"),
                name = " ",
                countryCode = "FR",
                isCreditAgricole = false
            )
        }
    }

    @Test
    fun `Country code must be two uppercase letters`() {
        assertFailsWith<IllegalArgumentException> {
            Bank(
                id = BankId("bank-1"),
                name = "Banque",
                countryCode = "fra",
                isCreditAgricole = false
            )
        }

        assertFailsWith<IllegalArgumentException> {
            Bank(
                id = BankId("bank-1"),
                name = "Banque",
                countryCode = "F1",
                isCreditAgricole = false
            )
        }
    }
}

class AccountTest {

    @Test
    fun `Account name cannot be blank`() {
        assertFailsWith<IllegalArgumentException> {
            Account(
                id = AccountId("account-1"),
                bankId = BankId("bank-1"),
                name = "",
                balance = Money(0, "EUR"),
                kind = AccountKind.CHECKING
            )
        }
    }
}

class OperationTest {

    @Test
    fun `Debit operation requires negative amount`() {
        assertFailsWith<IllegalArgumentException> {
            Operation(
                id = OperationId("op-1"),
                accountId = AccountId("account-1"),
                description = "Payment",
                amount = Money(100, "EUR"),
                type = OperationType.DEBIT,
                executedAtEpochMillis = 1_000L
            )
        }
    }

    @Test
    fun `Credit operation requires positive amount`() {
        assertFailsWith<IllegalArgumentException> {
            Operation(
                id = OperationId("op-2"),
                accountId = AccountId("account-1"),
                description = "Refund",
                amount = Money(-100, "EUR"),
                type = OperationType.CREDIT,
                executedAtEpochMillis = 1_000L
            )
        }
    }

    @Test
    fun `Valid debit and credit operations are accepted`() {
        val debit = Operation(
            id = OperationId("op-3"),
            accountId = AccountId("account-1"),
            description = "Purchase",
            amount = Money(-500, "EUR"),
            type = OperationType.DEBIT,
            executedAtEpochMillis = 1_000L
        )
        val credit = Operation(
            id = OperationId("op-4"),
            accountId = AccountId("account-1"),
            description = "Salary",
            amount = Money(2_000, "EUR"),
            type = OperationType.CREDIT,
            executedAtEpochMillis = 2_000L
        )

        assertTrue(debit.amount.isNegative)
        assertFalse(credit.amount.isNegative)
    }
}

class IdentifierTest {

    @Test
    fun `Identifiers reject blank values`() {
        assertFailsWith<IllegalArgumentException> { BankId(" ") }
        assertFailsWith<IllegalArgumentException> { AccountId("") }
        assertFailsWith<IllegalArgumentException> { OperationId("   ") }
    }
}
