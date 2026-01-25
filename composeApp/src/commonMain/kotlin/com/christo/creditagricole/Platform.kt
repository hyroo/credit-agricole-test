package com.christo.creditagricole

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform