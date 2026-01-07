package com.nano.ai.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.nano.ai.di.AppContainer
import com.nano.ai.navigation.AppNavHost
import com.nano.ai.ui.theme.NanoAiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel = AppContainer.getSettingsViewModel()
            val darkMode by settingsViewModel.darkMode.collectAsStateWithLifecycle()
            val dynamicColor by settingsViewModel.dynamicColor.collectAsStateWithLifecycle()

            val navController = rememberNavController()

            NanoAiTheme(
                darkTheme = darkMode,
                dynamicColor = dynamicColor
            ) {
                AppNavHost(navController = navController)
            }
        }
    }
}
