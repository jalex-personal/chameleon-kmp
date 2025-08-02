package com.chameleon.network

import com.chameleon.game.GameState
import kotlinx.coroutines.flow.SharedFlow

interface GameServerInterface {
    val gameStateFlow: SharedFlow<GameState>
    
    fun startServer(port: Int = 8080): String?
    fun stopServer()
    fun startGame(playerNames: List<String>)
}
