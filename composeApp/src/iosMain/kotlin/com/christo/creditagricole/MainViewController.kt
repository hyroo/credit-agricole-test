package com.christo.creditagricole

import androidx.compose.ui.window.ComposeUIViewController
import com.christo.creditagricole.di.initKoin

fun MainViewController() = ComposeUIViewController {
    initKoin()
    App()
}
