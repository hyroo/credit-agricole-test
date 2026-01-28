package com.christo.creditagricole.di

import com.christo.creditagricole.core.DefaultDispatcherProvider
import com.christo.creditagricole.core.DispatcherProvider
import com.christo.creditagricole.data.api.BankingApi
import com.christo.creditagricole.data.network.NetworkClientFactory
import com.christo.creditagricole.data.network.createBankingApi
import com.christo.creditagricole.data.network.defaultJson
import com.christo.creditagricole.data.repository.AccountRepositoryImpl
import com.christo.creditagricole.data.repository.BankRepositoryImpl
import com.christo.creditagricole.data.repository.OperationRepositoryImpl
import com.christo.creditagricole.domain.model.Account
import com.christo.creditagricole.domain.repository.AccountRepository
import com.christo.creditagricole.domain.repository.BankRepository
import com.christo.creditagricole.domain.repository.OperationRepository
import com.christo.creditagricole.domain.usecase.GetAccountsForBankUseCase
import com.christo.creditagricole.domain.usecase.GetBanksUseCase
import com.christo.creditagricole.domain.usecase.GetMockBanksUseCase
import com.christo.creditagricole.domain.usecase.GetOperationsForAccountUseCase
import com.christo.creditagricole.presentation.features.accounts.AccountListStrings
import com.christo.creditagricole.presentation.features.accounts.AccountListViewModel
import com.christo.creditagricole.presentation.features.accounts.DefaultAccountListStrings
import com.christo.creditagricole.presentation.features.detail.AccountDetailStrings
import com.christo.creditagricole.presentation.features.detail.AccountDetailViewModel
import com.christo.creditagricole.presentation.features.detail.DefaultAccountDetailStrings
import com.christo.creditagricole.presentation.features.detail.AccountSelectionStore
import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

const val PROPERTY_BASE_URL = "base_url"
private const val BASE_URL =
    "https://cdf-test-mobile-default-rtdb.europe-west1.firebasedatabase.app/"

private val coreModule = module {
    single<DispatcherProvider> { DefaultDispatcherProvider }
}
private val dataModule = module {
    single { defaultJson() }
    single<Ktorfit> {
        val baseUrl: String = getKoin().getProperty(PROPERTY_BASE_URL) ?: BASE_URL
        NetworkClientFactory.createKtorfit(baseUrl = baseUrl, json = get())
    }
    single<BankingApi> { get<Ktorfit>().createBankingApi() }
    single<BankRepository> { BankRepositoryImpl(api = get()) }
    single<AccountRepository> { AccountRepositoryImpl(api = get()) }
    single<OperationRepository> { OperationRepositoryImpl(api = get()) }
}

private val domainModule = module {
    factory { GetBanksUseCase(bankRepository = get()) }
    factory { GetMockBanksUseCase(bankRepository = get()) }
    factory { GetAccountsForBankUseCase(accountRepository = get()) }
    factory { GetOperationsForAccountUseCase(operationRepository = get()) }
}

private val presentationModule = module {
    single { AccountSelectionStore() }
    single {
        AccountListViewModel(
            dispatcherProvider = get(),
            getBanksUseCase = get(),
            getMockBanksUseCase = get(),
            getAccountsForBankUseCase = get(),
            strings = get()
        )
    }
    factory<AccountListStrings> { DefaultAccountListStrings() }
    factory { (bankName: String, account: Account) ->
        AccountDetailViewModel(
            dispatcherProvider = get(),
            bankName = bankName,
            account = account,
            getOperationsForAccountUseCase = get(),
            strings = get()
        )
    }
    factory<AccountDetailStrings> { DefaultAccountDetailStrings() }
}

val sharedModules: List<Module> = listOf(
    coreModule,
    dataModule,
    domainModule,
    presentationModule,
)

fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication = startKoin {
    modules(sharedModules)
    appDeclaration()
}
