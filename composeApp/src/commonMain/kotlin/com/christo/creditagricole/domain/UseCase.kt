package com.christo.creditagricole.domain

fun interface SuspendUseCase<in P, out R> {
    suspend operator fun invoke(params: P): R
}

interface NoParamSuspendUseCase<out R> : SuspendUseCase<Unit, R> {
    suspend operator fun invoke(): R = invoke(Unit)
}
