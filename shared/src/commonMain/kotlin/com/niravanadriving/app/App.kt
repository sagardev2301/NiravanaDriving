package com.niravanadriving.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.niravanadriving.app.ui.navigation.AppNavigation
import com.niravanadriving.app.ui.theme.NirvanaDriveTheme

@Composable
fun App() {
    NirvanaDriveTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            AppNavigation()
        }
    }
}
