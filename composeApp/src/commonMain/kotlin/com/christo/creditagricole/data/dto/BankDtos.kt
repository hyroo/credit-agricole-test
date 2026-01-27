package com.christo.creditagricole.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BankDto(
    @SerialName("name") val name: String,
    @SerialName("isCA") val isCaFlag: Int? = null,
    @SerialName("accounts") val accounts: List<AccountDto> = emptyList(),
    @SerialName("id") val id: String? = null,
    @SerialName("country_code") val countryCode: String? = null
)

@Serializable
data class AccountDto(
    @SerialName("id") val id: String,
    @SerialName("label") val label: String? = null,
    @SerialName("balance") val balance: Double? = null,
    @SerialName("order") val order: Int? = null,
    @SerialName("holder") val holder: String? = null,
    @SerialName("role") val role: Int? = null,
    @SerialName("contract_number") val contractNumber: String? = null,
    @SerialName("product_code") val productCode: String? = null,
    @SerialName("operations") val operations: List<OperationDto> = emptyList(),
    @SerialName("bank_id") val bankId: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("kind") val kind: String? = null
)

@Serializable
data class OperationDto(
    @SerialName("id") val id: String,
    @SerialName("amount") val amount: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("category") val category: String? = null,
    @SerialName("date") val date: String? = null,
    @SerialName("account_id") val accountId: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("type") val type: String? = null
)

@Serializable
data class BanksResponseDto(
    @SerialName("banks") val banks: List<BankDto>
)

@Serializable
data class AccountsResponseDto(
    @SerialName("accounts") val accounts: List<AccountDto>
)

@Serializable
data class OperationsResponseDto(
    @SerialName("operations") val operations: List<OperationDto>
)
