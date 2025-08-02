package com.chameleon.game

import kotlinx.serialization.Serializable

@Serializable
enum class GamePhase {
    SETUP,
    GIVING_CLUES,
    DISCUSSION,
    VOTING,
    CHAMELEON_GUESS,
    ROUND_END,
    GAME_END
}

@Serializable
data class GameState(
    val players: List<Player> = emptyList(),
    val currentRound: Int = 1,
    val phase: GamePhase = GamePhase.SETUP,
    val secretWord: String = "",
    val topicCard: TopicCard? = null,
    val clues: Map<String, String> = emptyMap(), // playerId to clue
    val votes: Map<String, String> = emptyMap(), // voter playerId to voted playerId
    val chameleonPlayerId: String = "",
    val currentPlayerIndex: Int = 0,
    val maxScore: Int = 5
) {
    val currentPlayer: Player?
        get() = players.getOrNull(currentPlayerIndex)
    
    val chameleonPlayer: Player?
        get() = players.find { it.id == chameleonPlayerId }
    
    val isGameOver: Boolean
        get() = players.any { it.score >= maxScore }
}
