package com.christo.creditagricole.data.mapper

import com.christo.creditagricole.data.dto.AccountDto
import com.christo.creditagricole.data.dto.BankDto
import com.christo.creditagricole.data.dto.OperationDto
import com.christo.creditagricole.data.exceptions.DataMappingException
import com.christo.creditagricole.domain.model.Account
import com.christo.creditagricole.domain.model.AccountId
import com.christo.creditagricole.domain.model.AccountKind
import com.christo.creditagricole.domain.model.Bank
import com.christo.creditagricole.domain.model.BankId
import com.christo.creditagricole.domain.model.Money
import com.christo.creditagricole.domain.model.Operation
import com.christo.creditagricole.domain.model.OperationId
import com.christo.creditagricole.domain.model.OperationType
import kotlin.math.absoluteValue
import kotlin.math.roundToLong

private const val DEFAULT_COUNTRY_CODE = "FR"
private const val DEFAULT_CURRENCY_CODE = "EUR"

internal fun BankDto.toDomain(): Bank = runCatching {
    val resolvedName = name.takeIf { it.isNotBlank() }
        ?: throw DataMappingException("Le nom de la banque est obligatoire.")

    val resolvedIdValue = (id?.takeIf { it.isNotBlank() } ?: resolvedName.slugified())
        .ifBlank { resolvedName.slugified() }

    val resolvedCountryCode = countryCode
        ?.takeIf { it.length == 2 && it.all { char -> char.isLetter() } }
        ?.uppercase()
        ?: DEFAULT_COUNTRY_CODE

    val isCreditAgricole = when (isCaFlag) {
        null -> inferCreditAgricoleFlag(resolvedName)
        else -> isCaFlag != 0
    }

    Bank(
        id = BankId(resolvedIdValue),
        name = resolvedName,
        countryCode = resolvedCountryCode,
        isCreditAgricole = isCreditAgricole
    )
}.getOrElse { throwable ->
    throw DataMappingException("Impossible de mapper la banque ${id ?: name}", throwable)
}

internal fun AccountDto.toDomain(fallbackBankId: BankId? = null): Account = runCatching {
    val resolvedBankId = bankId
        ?.takeIf { it.isNotBlank() }
        ?.let(::BankId)
        ?: fallbackBankId
        ?: throw DataMappingException("Bank id manquant pour le compte $id")

    val displayName = listOfNotNull(label, name, holder)
        .firstOrNull { it.isNotBlank() }
        ?: "Compte $id"

    val amountMinor = balance
        ?.let { (it * 100.0).roundToLong() }
        ?: throw DataMappingException("Solde manquant pour le compte $id")

    val money = runCatching {
        Money(
            amountInMinor = amountMinor,
            currencyCode = DEFAULT_CURRENCY_CODE
        )
    }.getOrElse { throwable ->
        throw DataMappingException("Montant invalide pour le compte $id", throwable)
    }

    val accountKind = kind?.toDomainAccountKind() ?: AccountKind.OTHER

    Account(
        id = AccountId(id),
        bankId = resolvedBankId,
        name = displayName,
        balance = money,
        kind = accountKind
    )
}.getOrElse { throwable ->
    throw DataMappingException("Impossible de mapper le compte $id", throwable)
}

internal fun OperationDto.toDomain(fallbackAccountId: AccountId? = null): Operation = runCatching {
    val resolvedAccountId = accountId
        ?.takeIf { it.isNotBlank() }
        ?.let(::AccountId)
        ?: fallbackAccountId
        ?: throw DataMappingException("Account id manquant pour l'operation $id")

    val rawDescription = description?.takeIf { it.isNotBlank() }
        ?: title?.takeIf { it.isNotBlank() }
        ?: "Operation $id"

    val rawAmount = amount?.takeIf { it.isNotBlank() }
        ?: throw DataMappingException("Montant manquant pour l'operation $id")

    val parsedAmountMinor = rawAmount.toMinorUnits()

    val resolvedType = type?.toDomainOperationTypeOrNull()
        ?: inferOperationType(parsedAmountMinor)

    val signedAmount = parsedAmountMinor.ensureSignFor(resolvedType)

    val executedAt = date.toEpochMillis()

    val money = runCatching {
        Money(
            amountInMinor = signedAmount,
            currencyCode = DEFAULT_CURRENCY_CODE
        )
    }.getOrElse { throwable ->
        throw DataMappingException("Montant invalide pour l'operation $id", throwable)
    }

    Operation(
        id = OperationId(id),
        accountId = resolvedAccountId,
        description = rawDescription,
        amount = money,
        type = resolvedType,
        executedAtEpochMillis = executedAt
    )
}.getOrElse { throwable ->
    throw DataMappingException("Impossible de mapper l'operation $id", throwable)
}

private fun String.slugified(): String {
    val slug = lowercase()
        .replace("[^a-z0-9]+".toRegex(), "-")
        .trim('-')
    return if (slug.isNotEmpty()) slug else "bank-${hashCode().absoluteValue}"
}

private fun inferCreditAgricoleFlag(name: String): Boolean =
    name.contains("crédit agricole", ignoreCase = true) ||
            name.contains("ca ", ignoreCase = true)

private fun String.toDomainAccountKind(): AccountKind =
    AccountKind.entries.firstOrNull { it.name.equals(this, ignoreCase = true) } ?: AccountKind.OTHER

private fun String?.toDomainOperationTypeOrNull(): OperationType? =
    this?.let { value ->
        OperationType.entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }

private fun inferOperationType(amountMinor: Long): OperationType =
    if (amountMinor < 0) OperationType.DEBIT else OperationType.CREDIT

private fun Long.ensureSignFor(type: OperationType): Long = when (type) {
    OperationType.CREDIT -> absoluteValue
    OperationType.DEBIT -> -absoluteValue
}

private fun String.toMinorUnits(): Long {
    val normalized = trim().replace(" ", "")
    if (normalized.isEmpty()) throw DataMappingException("Montant vide")

    val sign = when {
        normalized.startsWith("-") -> -1
        normalized.startsWith("+") -> 1
        else -> 1
    }

    val unsigned = normalized
        .removePrefix("-")
        .removePrefix("+")
        .replace(',', '.')

    val numeric = unsigned.toDoubleOrNull()
        ?: throw DataMappingException("Montant invalide: $this")

    return (numeric * 100.0).roundToLong() * sign
}

private fun String?.toEpochMillis(): Long {
    val raw = this?.trim().orEmpty()
    val numeric = raw.toLongOrNull() ?: return 0L
    return if (raw.length <= 10) numeric * 1_000 else numeric
}
