package com.chameleon.network

import com.chameleon.game.GameState
import kotlinx.serialization.Serializable

@Serializable
sealed class NetworkMessage {
    @Serializable
    data class JoinGameRequest(val playerName: String) : NetworkMessage()
    
    @Serializable
    data class JoinGameResponse(val success: Boolean, val playerId: String?, val message: String) : NetworkMessage()
    
    @Serializable
    data class GameStateUpdate(val gameState: GameState) : NetworkMessage()
    
    @Serializable
    data class PlayerAction(
        val playerId: String,
        val action: PlayerActionType,
        val data: String = ""
    ) : NetworkMessage()
    
    @Serializable
    data class PlayerLeft(val playerId: String, val playerName: String) : NetworkMessage()
    
    @Serializable
    object GameStarted : NetworkMessage()
    
    @Serializable
    data class Error(val message: String) : NetworkMessage()
}

@Serializable
enum class PlayerActionType {
    SUBMIT_CLUE,
    SUBMIT_VOTE,
    SUBMIT_CHAMELEON_GUESS,
    START_VOTING,
    READY_FOR_NEXT_ROUND
}
