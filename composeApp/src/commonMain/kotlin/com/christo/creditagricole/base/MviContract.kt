package com.christo.creditagricole.base

interface MviIntent

interface MviState

interface MviEffect

fun interface MviReducer<S : MviState, in R> {
    fun reduce(currentState: S, result: R): S
}
