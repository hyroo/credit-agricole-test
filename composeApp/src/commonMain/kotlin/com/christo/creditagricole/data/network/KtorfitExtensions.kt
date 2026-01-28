package com.christo.creditagricole.data.network

import com.christo.creditagricole.data.api.BankingApi
import com.christo.creditagricole.data.dto.AccountDto
import com.christo.creditagricole.data.dto.AccountsResponseDto
import com.christo.creditagricole.data.dto.BankDto
import com.christo.creditagricole.data.dto.BanksSnapshotDto
import com.christo.creditagricole.data.dto.OperationDto
import com.christo.creditagricole.data.dto.OperationsResponseDto
import creditagricole.composeapp.generated.resources.Res
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.appendPathSegments
import io.ktor.http.takeFrom
import kotlin.math.absoluteValue
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * Local replacement for Ktorfit generated code. The interface retains the
 * Ktorfit annotations but we provide the runtime implementation manually to
 * keep the data layer testable without the compiler plugin.
 *
 * This implementation keeps a small in-memory cache of the banks payload so
 * that subsequent calls that need the bank hierarchy can reuse the snapshot
 * instead of re-hitting the backend each time.
 */
fun Ktorfit.createBankingApi(): BankingApi {
    val ktorfit = this
    return object : BankingApi {

        private val client = ktorfit.httpClient
        private val baseUrl = ktorfit.baseUrl.trimEnd('/')
        private val banksCacheMutex = Mutex()
        private var cachedBanks: List<BankDto>? = null
        private val json: Json = defaultJson()

        override suspend fun getBanks(): List<BankDto> =
            fetchBanksSnapshot(tag = "getBanks")

        override suspend fun getAccountsForBank(bankId: String): AccountsResponseDto {
            println("BankingApi#getAccountsForBank -> bankId=$bankId")
            val banks = fetchBanksSnapshot(tag = "getAccountsForBank")
            val bank = banks.firstOrNull { it.resolveId() == bankId }
            val resolvedBankId = bank?.resolveId()
            val accounts = bank
                ?.accounts
                .orEmpty()
                .map { dto -> resolvedBankId?.let { dto.withBankId(it) } ?: dto }
            println("BankingApi#getAccountsForBank <- ${accounts.size} accounts")
            return AccountsResponseDto(accounts)
        }

        override suspend fun getAccount(accountId: String): AccountDto {
            println("BankingApi#getAccount -> accountId=$accountId")
            val banks = fetchBanksSnapshot(tag = "getAccount")
            val (bank, account) = banks
                .asSequence()
                .flatMap { bank -> bank.accounts.asSequence().map { account -> bank to account } }
                .firstOrNull { (_, account) -> account.id == accountId }
                ?: throw IllegalStateException("Account $accountId introuvable")
            val resolvedBankId = bank.resolveId()
            return account
                .withBankId(resolvedBankId)
                .also { println("BankingApi#getAccount <- bankId=$resolvedBankId") }
        }

        override suspend fun getOperationsForAccount(accountId: String): OperationsResponseDto {
            println("BankingApi#getOperationsForAccount -> accountId=$accountId")
            val banks = fetchBanksSnapshot(tag = "getOperationsForAccount")
            val account = banks
                .asSequence()
                .flatMap { it.accounts.asSequence() }
                .firstOrNull { it.id == accountId }
                ?: throw IllegalStateException("Account $accountId introuvable pour les operations")
            val operations = account.operations
                .map { it.withAccountId(accountId) }
            println("BankingApi#getOperationsForAccount <- ${operations.size} operations")
            return OperationsResponseDto(operations)
        }

        override suspend fun getOperation(operationId: String): OperationDto {
            println("BankingApi#getOperation -> operationId=$operationId")
            val banks = fetchBanksSnapshot(tag = "getOperation")
            val (account, operation) = banks
                .asSequence()
                .flatMap { bank ->
                    bank.accounts.asSequence().flatMap { account ->
                        account.operations.asSequence().map { operation -> account to operation }
                    }
                }
                .firstOrNull { (_, operation) -> operation.id == operationId }
                ?: throw IllegalStateException("Operation $operationId introuvable")
            val resolvedAccountId = account.id
            return operation
                .withAccountId(resolvedAccountId)
                .also { println("BankingApi#getOperation <- accountId=$resolvedAccountId") }
        }

        private suspend fun fetchBanksSnapshot(
            tag: String,
            forceRefresh: Boolean = false
        ): List<BankDto> {
            return banksCacheMutex.withLock {
                if (!forceRefresh) {
                    cachedBanks?.also { banks ->
                        println("BankingApi#$tag <- ${banks.size} banks served from cache")
                        return@withLock banks
                    }
                }

                val banks = runCatching { loadRemoteBanks(tag) }
                    .recoverCatching { error ->
                        println("BankingApi#$tag !! remote fetch failed: ${error.message}. Loading fallback banks.json")
                        loadBundledBanks(tag, error)
                    }
                    .getOrElse { throw it }

                cachedBanks = banks
                println("BankingApi#$tag <- ${banks.size} banks cached")
                banks
            }
        }

        private suspend fun loadRemoteBanks(tag: String): List<BankDto> {
            val url = "$baseUrl/banks.json"
            println("BankingApi#$tag -> GET $url")
            val response = client.get {
                url {
                    takeFrom(baseUrl)
                    appendPathSegments("banks.json")
                }
            }
            println("BankingApi#$tag <- http ${response.status}")
            return response.body<List<BankDto>>()
        }

        @OptIn(ExperimentalResourceApi::class)
        private suspend fun loadBundledBanks(
            tag: String,
            cause: Throwable? = null
        ): List<BankDto> {
            return runCatching {
                val bytes = Res.readBytes("files/banks.json")
                val snapshot = json.decodeFromString<BanksSnapshotDto>(bytes.decodeToString())
                println("BankingApi#$tag <- fallback provided ${snapshot.banks.size} banks")
                snapshot.banks
            }.getOrElse { fallbackError ->
                cause?.let {
                    fallbackError.addSuppressed(it)
                }
                println("BankingApi#$tag !! fallback failed: ${fallbackError.message}")
                throw fallbackError
            }
        }

        private fun BankDto.resolveId(): String {
            val rawId = id?.takeIf { it.isNotBlank() } ?: name.slugified()
            return rawId.ifBlank { name.slugified() }
        }

        private fun AccountDto.withBankId(bankId: String): AccountDto =
            if (this.bankId == bankId) this else copy(bankId = bankId)

        private fun OperationDto.withAccountId(accountId: String): OperationDto =
            if (this.accountId == accountId) this else copy(accountId = accountId)

        private fun String.slugified(): String {
            val slug = lowercase()
                .replace("[^a-z0-9]+".toRegex(), "-")
                .trim('-')
            return if (slug.isNotEmpty()) slug else "bank-${hashCode().absoluteValue}"
        }
    }
}
