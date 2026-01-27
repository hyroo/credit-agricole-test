package com.christo.creditagricole

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.christo.creditagricole.designsystem.theme.ColorScheme
import com.christo.creditagricole.designsystem.theme.Theme
import com.christo.creditagricole.di.initKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        initKoin()
        setContent {
            App()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AccountsScreenPreview() {
    Theme(colors = ColorScheme.Light) {
        App()
    }
}
