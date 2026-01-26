package com.christo.creditagricole.domain.model

data class Bank(
    val id: BankId,
    val name: String,
    val countryCode: String
) {
    init {
        require(name.isNotBlank()) { "Bank name cannot be blank." }
        require(countryCode.length == 2 && countryCode.all { it.isLetter() }) {
            "Country code must follow the ISO-3166 alpha-2 format."
        }
        require(countryCode == countryCode.uppercase()) {
            "Country code must be provided using upper-case letters."
        }
    }
}
