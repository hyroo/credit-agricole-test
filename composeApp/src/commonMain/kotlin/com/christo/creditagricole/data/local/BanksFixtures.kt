package com.christo.creditagricole.data.local

import com.christo.creditagricole.data.dto.AccountDto
import com.christo.creditagricole.data.dto.BankDto
import com.christo.creditagricole.data.dto.BanksResponseDto
import com.christo.creditagricole.data.dto.OperationDto

internal object BanksFixtures {

    val response: BanksResponseDto by lazy {
        BanksResponseDto(
            banks = listOf(
                BankDto(
                    name = "CA Languedoc",
                    isCaFlag = 1,
                    accounts = listOf(
                        AccountDto(
                            order = 1,
                            id = "151515151151",
                            holder = "Corinne Martin",
                            role = 1,
                            contractNumber = "32216549871",
                            label = "Compte de dépôt",
                            productCode = "00004",
                            balance = 2031.84,
                            operations = listOf(
                                OperationDto(
                                    id = "2",
                                    title = "Prélèvement Netflix",
                                    amount = "-15,99",
                                    category = "leisure",
                                    date = "1644870724"
                                ),
                                OperationDto(
                                    id = "4",
                                    title = "CB Amazon",
                                    amount = "-95,99",
                                    category = "online",
                                    date = "1644611558"
                                )
                            )
                        ),
                        AccountDto(
                            order = 2,
                            id = "9892736780987654",
                            holder = "M. et Mme Martin",
                            role = 2,
                            contractNumber = "09320939231",
                            label = "Compte joint",
                            productCode = "00007",
                            balance = 843.15,
                            operations = listOf(
                                OperationDto(
                                    id = "2",
                                    title = "Prélèvement Netflix",
                                    amount = "-15,99",
                                    category = "leisure",
                                    date = "1644784369"
                                ),
                                OperationDto(
                                    id = "3",
                                    title = "Prélèvement Century 21",
                                    amount = "-750,00",
                                    category = "housing",
                                    date = "1644179569"
                                )
                            )
                        ),
                        AccountDto(
                            order = 3,
                            id = "2354657678098765",
                            holder = "Thaïs Martin",
                            role = 6,
                            contractNumber = "29389382872",
                            label = "Compte Mozaïc",
                            productCode = "00007",
                            balance = 209.39,
                            operations = listOf(
                                OperationDto(
                                    id = "2",
                                    title = "Orange",
                                    amount = "-15,99",
                                    category = "leisure",
                                    date = "1644438769"
                                )
                            )
                        )
                    )
                ),
                BankDto(
                    name = "Boursorama",
                    isCaFlag = 0,
                    accounts = listOf(
                        AccountDto(
                            order = 1,
                            id = "09878900000",
                            holder = "Corinne Martin",
                            role = 1,
                            contractNumber = "32216549871",
                            label = "Compte de dépôt",
                            productCode = "00004",
                            balance = 45.84,
                            operations = listOf(
                                OperationDto(
                                    id = "2",
                                    title = "Tenue de compte",
                                    amount = "-1,99",
                                    category = "costs",
                                    date = "1588690878"
                                ),
                                OperationDto(
                                    id = "3",
                                    title = "Tenue de compte",
                                    amount = "-1,99",
                                    category = "costs",
                                    date = "1641760369"
                                )
                            )
                        )
                    )
                ),
                BankDto(
                    name = "Banque Pop",
                    isCaFlag = 0,
                    accounts = listOf(
                        AccountDto(
                            order = 1,
                            id = "3982997777",
                            holder = "Jean Martin",
                            role = 1,
                            contractNumber = "32216549871",
                            label = "Compte Chèques",
                            productCode = "00004",
                            balance = 675.04,
                            operations = listOf(
                                OperationDto(
                                    id = "2",
                                    title = "Prêt immo",
                                    amount = "-1331,44",
                                    category = "costs",
                                    date = "1644179569"
                                ),
                                OperationDto(
                                    id = "2",
                                    title = "CB La Vie Claire",
                                    amount = "-53,20",
                                    category = "food",
                                    date = "1644784369"
                                ),
                                OperationDto(
                                    id = "3",
                                    title = "Prélèvement Spotify",
                                    amount = "-10,00",
                                    category = "leisure",
                                    date = "1644611558"
                                ),
                                OperationDto(
                                    id = "4",
                                    title = "CB Billets SNCF",
                                    amount = "-53,00",
                                    category = "trip",
                                    date = "1644870724"
                                )
                            )
                        )
                    )
                ),
                BankDto(
                    name = "CA Centre-Est",
                    isCaFlag = 1,
                    accounts = listOf(
                        AccountDto(
                            order = 1,
                            id = "3982938",
                            holder = "Corinne Martin",
                            role = 1,
                            contractNumber = "32216549871",
                            label = "Compte de dépôt",
                            productCode = "00004",
                            balance = 425.84,
                            operations = listOf(
                                OperationDto(
                                    id = "2",
                                    title = "Tenue de compte",
                                    amount = "-1,99",
                                    category = "costs",
                                    date = "1644870724"
                                ),
                                OperationDto(
                                    id = "2",
                                    title = "Prélèvement Orange",
                                    amount = "-45,99",
                                    category = "leisure",
                                    date = "1644870724"
                                )
                            )
                        )
                    )
                )
            )
        )
    }
}
