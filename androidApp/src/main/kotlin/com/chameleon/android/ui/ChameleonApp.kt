package com.chameleon.android.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chameleon.android.ui.screens.GameScreen
import com.chameleon.android.ui.screens.SetupScreen

@Composable
fun ChameleonApp() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "setup"
    ) {
        composable("setup") {
            SetupScreen(
                onStartGame = { playerNames ->
                    navController.navigate("game")
                }
            )
        }
        composable("game") {
            GameScreen(
                onBackToSetup = {
                    navController.popBackStack()
                }
            )
        }
    }
}
