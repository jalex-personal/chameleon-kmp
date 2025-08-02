package com.chameleon.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.chameleon.game.GameEngine
import com.chameleon.game.GameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel : ViewModel() {
    private val gameEngine = GameEngine()
    
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    
    fun startNewGame(playerNames: List<String>) {
        val newState = gameEngine.startNewGame(playerNames)
        _gameState.value = newState
    }
    
    fun startNewRound() {
        gameEngine.startNewRound()
        _gameState.value = gameEngine.gameState
    }
    
    fun submitClue(playerId: String, clue: String) {
        val newState = gameEngine.submitClue(playerId, clue)
        _gameState.value = newState
    }
    
    fun startVoting() {
        val newState = gameEngine.startVoting()
        _gameState.value = newState
    }
    
    fun submitVote(voterPlayerId: String, votedPlayerId: String) {
        val newState = gameEngine.submitVote(voterPlayerId, votedPlayerId)
        _gameState.value = newState
    }
    
    fun submitChameleonGuess(guess: String) {
        val newState = gameEngine.submitChameleonGuess(guess)
        _gameState.value = newState
    }
    
    fun getSecretWordForPlayer(playerId: String): String? {
        return gameEngine.getSecretWordForPlayer(playerId)
    }
}
