package com.chameleon.android.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chameleon.android.ui.screens.*
import com.chameleon.android.ui.viewmodel.NetworkGameViewModel

@Composable
fun ChameleonApp() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "network_setup"
    ) {
        composable("network_setup") {
            NetworkSetupScreen(
                onHostGame = {
                    navController.navigate("host_game")
                },
                onJoinGame = {
                    navController.navigate("join_game")
                },
                onLocalGame = {
                    navController.navigate("local_setup")
                }
            )
        }
        
        composable("local_setup") {
            SetupScreen(
                onStartGame = { playerNames ->
                    navController.navigate("local_game")
                }
            )
        }
        
        composable("local_game") {
            GameScreen(
                onBackToSetup = {
                    navController.popBackStack("network_setup", false)
                }
            )
        }
        
        composable("host_game") {
            val networkViewModel: NetworkGameViewModel = viewModel()
            HostGameScreen(
                onBackToSetup = {
                    navController.popBackStack("network_setup", false)
                },
                onStartGame = {
                    navController.navigate("network_game")
                },
                viewModel = networkViewModel
            )
        }
        
        composable("join_game") {
            val networkViewModel: NetworkGameViewModel = viewModel()
            JoinGameScreen(
                onBackToSetup = {
                    navController.popBackStack("network_setup", false)
                },
                onJoinedGame = {
                    navController.navigate("lobby")
                },
                viewModel = networkViewModel
            )
        }
        
        composable("lobby") {
            val networkViewModel: NetworkGameViewModel = viewModel()
            LobbyScreen(
                onBackToSetup = {
                    navController.popBackStack("network_setup", false)
                },
                onGameStarted = {
                    navController.navigate("network_game")
                },
                viewModel = networkViewModel
            )
        }
        
        composable("network_game") {
            val networkViewModel: NetworkGameViewModel = viewModel()
            NetworkGameScreen(
                onBackToSetup = {
                    navController.popBackStack("network_setup", false)
                },
                viewModel = networkViewModel
            )
        }
    }
}
