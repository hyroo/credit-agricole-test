package com.christo.creditagricole.presentation.features.detail

import com.christo.creditagricole.domain.model.OperationId
import org.jetbrains.compose.resources.StringResource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AccountDetailReducerTest {

    @Test
    fun `Idle result keeps state unchanged`() {
        val initialState = state()

        val newState = AccountDetailReducer.reduce(initialState, AccountDetailResult.Idle)

        assertSame(initialState, newState)
    }

    @Test
    fun `Loading result toggles loading flag and clears transient fields`() {
        val initialState = state(
            isLoading = false,
            errorMessage = "Error",
            navigationTarget = AccountDetailNavigation.Back
        )

        val newState = AccountDetailReducer.reduce(initialState, AccountDetailResult.Loading)

        assertTrue(newState.isLoading)
        assertEquals(null, newState.errorMessage)
        assertEquals(null, newState.navigationTarget)
    }

    @Test
    fun `OperationsContent result populates operations and marks state as loaded`() {
        val initialState = state(
            isLoading = true,
            hasLoaded = false,
            operations = emptyList()
        )
        val operations = listOf(operation())

        val newState = AccountDetailReducer.reduce(
            initialState,
            AccountDetailResult.OperationsContent(operations)
        )

        assertFalse(newState.isLoading)
        assertEquals(operations, newState.operations)
        assertEquals(null, newState.errorMessage)
        assertTrue(newState.hasLoaded)
    }

    @Test
    fun `Error result propagates error and stops loading`() {
        val initialState = state(
            isLoading = true,
            hasLoaded = false,
            errorMessage = null
        )
        val errorMessage = "Boom"

        val newState = AccountDetailReducer.reduce(
            initialState,
            AccountDetailResult.Error(errorMessage)
        )

        assertFalse(newState.isLoading)
        assertEquals(errorMessage, newState.errorMessage)
        assertTrue(newState.hasLoaded)
    }

    @Test
    fun `NavigateBack result exposes navigation target`() {
        val initialState = state(navigationTarget = null)

        val newState = AccountDetailReducer.reduce(
            initialState,
            AccountDetailResult.NavigateBack
        )

        assertIs<AccountDetailNavigation.Back>(newState.navigationTarget)
    }

    @Test
    fun `NavigationConsumed result clears navigation target`() {
        val initialState = state(navigationTarget = AccountDetailNavigation.Back)

        val newState = AccountDetailReducer.reduce(
            initialState,
            AccountDetailResult.NavigationConsumed
        )

        assertEquals(null, newState.navigationTarget)
    }

    private fun state(
        bankName: String = "Bank",
        accountName: String = "Account",
        accountKind: String = "Checking",
        balance: String = "100.00 €",
        operations: List<OperationItemUi> = emptyList(),
        isLoading: Boolean = false,
        errorMessage: String? = null,
        hasLoaded: Boolean = false,
        navigationTarget: AccountDetailNavigation? = null
    ): AccountDetailState = AccountDetailState(
        bankName = bankName,
        accountName = accountName,
        accountKind = accountKind,
        balance = balance,
        operations = operations,
        isLoading = isLoading,
        errorMessage = errorMessage,
        hasLoaded = hasLoaded,
        navigationTarget = navigationTarget
    )
    private fun operation(
        id: String = "op-1",
        description: String = "Operation 1",
        amount: String = "10.00 €",
        typeLabelRes: StringResource? = null,
        executedAt: String = "01/01/2024"
    ): OperationItemUi = OperationItemUi(
        id = OperationId(id),
        description = description,
        amount = amount,
        typeLabelRes = typeLabelRes,
        executedAt = executedAt
    )
}
