package com.chameleon.network

import com.chameleon.game.GameState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

actual class GameServer : GameServerInterface {
    private val _gameStateFlow = MutableSharedFlow<GameState>(replay = 1)
    override val gameStateFlow: SharedFlow<GameState> = _gameStateFlow
    
    override fun startServer(port: Int): String? {
        // iOS implementation would go here
        // For now, return null to indicate server not supported on iOS
        return null
    }
    
    override fun stopServer() {
        // iOS implementation would go here
    }
    
    override fun startGame(playerNames: List<String>) {
        // iOS implementation would go here
    }
}
