package com.christo.creditagricole.data.mapper

import com.christo.creditagricole.data.dto.AccountDto
import com.christo.creditagricole.data.dto.BankDto
import com.christo.creditagricole.data.dto.OperationDto
import com.christo.creditagricole.data.exceptions.DataMappingException
import com.christo.creditagricole.domain.model.AccountId
import com.christo.creditagricole.domain.model.AccountKind
import com.christo.creditagricole.domain.model.BankId
import com.christo.creditagricole.domain.model.OperationType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BankMapperTest {

    @Test
    fun `toDomain generates slug id and detects ca flag`() {
        val dto = BankDto(
            name = "Crédit Agricole Provence",
            isCaFlag = null,
            id = null,
            countryCode = "fr"
        )

        val bank = dto.toDomain()

        assertTrue(bank.id.value.isNotBlank())
        assertTrue(bank.isCreditAgricole)
        assertEquals("FR", bank.countryCode)
    }

    @Test
    fun `toDomain throws when name blank`() {
        val dto = BankDto(name = " ", id = "bank-1")

        assertFailsWith<DataMappingException> {
            dto.toDomain()
        }
    }

    @Test
    fun `explicit ca flag overrides inference`() {
        val dto = BankDto(
            name = "Banque Populaire",
            isCaFlag = 0
        )

        val bank = dto.toDomain()

        assertEquals("banque-populaire", bank.id.value)
        assertFalse(bank.isCreditAgricole)
    }
}

class AccountMapperTest {

    @Test
    fun `AccountDto maps with fallback bank id and label`() {
        val dto = AccountDto(
            id = "acc-1",
            label = "Compte Courant",
            balance = 1234.56,
            bankId = null,
            kind = "savings"
        )
        val fallbackBankId = BankId("bank-1")

        val account = dto.toDomain(fallbackBankId)

        assertEquals("acc-1", account.id.value)
        assertEquals(fallbackBankId, account.bankId)
        assertEquals("Compte Courant", account.name)
        assertEquals(123456, account.balance.amountInMinor)
        assertEquals(AccountKind.SAVINGS, account.kind)
    }

    @Test
    fun `AccountDto uses holder then generated name`() {
        val dto = AccountDto(
            id = "acc-2",
            balance = 10.0,
            holder = "John Doe",
            label = "",
            name = null,
            bankId = "bank-2"
        )

        val account = dto.toDomain()

        assertEquals("John Doe", account.name)
    }

    @Test
    fun `AccountDto throws when bank id missing and no fallback`() {
        val dto = AccountDto(
            id = "acc-3",
            balance = 42.0
        )

        assertFailsWith<DataMappingException> {
            dto.toDomain(null)
        }
    }

    @Test
    fun `AccountDto throws when balance missing`() {
        val dto = AccountDto(
            id = "acc-4",
            bankId = "bank-1"
        )

        assertFailsWith<DataMappingException> {
            dto.toDomain()
        }
    }
}

class OperationMapperTest {

    @Test
    fun `OperationDto maps with inferred type and signed amount`() {
        val dto = OperationDto(
            id = "op-1",
            amount = " -12,34 ",
            description = null,
            title = "Carte",
            accountId = null,
            type = null,
            date = "1700000000"
        )
        val fallbackAccountId = AccountId("account-1")

        val operation = dto.toDomain(fallbackAccountId)

        assertEquals(fallbackAccountId, operation.accountId)
        assertEquals("Carte", operation.description)
        assertEquals(OperationType.DEBIT, operation.type)
        assertEquals(-1234, operation.amount.amountInMinor)
        assertEquals(1_700_000_000_000L, operation.executedAtEpochMillis)
    }

    @Test
    fun `OperationDto enforces sign according to explicit type`() {
        val dto = OperationDto(
            id = "op-2",
            amount = "-42.00",
            type = "CREDIT",
            accountId = "account-2",
            description = "Virement",
            date = "1700005000000"
        )

        val operation = dto.toDomain()

        assertEquals(OperationType.CREDIT, operation.type)
        assertEquals(4200, operation.amount.amountInMinor)
    }

    @Test
    fun `OperationDto throws when account id missing and no fallback`() {
        val dto = OperationDto(
            id = "op-3",
            amount = "10",
            accountId = null
        )

        assertFailsWith<DataMappingException> {
            dto.toDomain(null)
        }
    }

    @Test
    fun `OperationDto throws on invalid amount`() {
        val dto = OperationDto(
            id = "op-4",
            amount = "abc",
            accountId = "account-4"
        )

        assertFailsWith<DataMappingException> {
            dto.toDomain()
        }
    }
}
