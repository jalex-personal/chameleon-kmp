package com.chameleon.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chameleon.game.GameState
import com.chameleon.network.GameClient
import com.chameleon.network.NetworkMessage
import com.chameleon.network.PlayerActionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NetworkGameUiState(
    val gameState: GameState = GameState(),
    val isConnected: Boolean = false,
    val connectionError: String? = null,
    val backendIp: String = "192.168.1.100",
    val backendPort: Int = 8080,
    val currentPlayerId: String? = null,
    val isLoading: Boolean = false
)

class NetworkGameViewModel : ViewModel() {
    private val gameClient = GameClient()
    
    private val _uiState = MutableStateFlow(NetworkGameUiState())
    val uiState: StateFlow<NetworkGameUiState> = _uiState.asStateFlow()
    
    init {
        observeNetworkMessages()
        observeConnectionState()
    }
    
    private fun observeNetworkMessages() {
        viewModelScope.launch {
            gameClient.messageFlow.collect { message ->
                when (message) {
                    is NetworkMessage.JoinGameResponse -> {
                        if (message.success) {
                            _uiState.value = _uiState.value.copy(
                                currentPlayerId = message.playerId,
                                connectionError = null,
                                isLoading = false
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                connectionError = message.message,
                                isLoading = false
                            )
                        }
                    }
                    is NetworkMessage.GameStateUpdate -> {
                        _uiState.value = _uiState.value.copy(
                            gameState = message.gameState
                        )
                    }
                    is NetworkMessage.Error -> {
                        _uiState.value = _uiState.value.copy(
                            connectionError = message.message,
                            isLoading = false
                        )
                    }
                    else -> {}
                }
            }
        }
    }
    
    private fun observeConnectionState() {
        viewModelScope.launch {
            gameClient.connectionStateFlow.collect { isConnected ->
                _uiState.value = _uiState.value.copy(
                    isConnected = isConnected,
                    connectionError = if (!isConnected) "Connection lost" else null
                )
            }
        }
    }
    
    fun createGame(playerName: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            connectionError = null
        )
        
        viewModelScope.launch {
            val success = gameClient.createGame(_uiState.value.backendIp, _uiState.value.backendPort, playerName)
            if (!success) {
                _uiState.value = _uiState.value.copy(
                    connectionError = "Failed to connect to backend server",
                    isLoading = false
                )
            }
        }
    }
    
    fun setBackendAddress(ip: String, port: Int) {
        _uiState.value = _uiState.value.copy(
            backendIp = ip,
            backendPort = port
        )
    }
    
    fun joinGame(playerName: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            connectionError = null
        )
        
        viewModelScope.launch {
            val success = gameClient.connect(_uiState.value.backendIp, _uiState.value.backendPort, playerName)
            if (!success) {
                _uiState.value = _uiState.value.copy(
                    connectionError = "Failed to connect to backend server",
                    isLoading = false
                )
            }
        }
    }
    
    fun startGame() {
        val playerId = _uiState.value.currentPlayerId ?: return
        
        viewModelScope.launch {
            gameClient.sendPlayerAction(playerId, PlayerActionType.READY_FOR_NEXT_ROUND)
        }
    }
    
    fun submitClue(clue: String) {
        val playerId = _uiState.value.currentPlayerId ?: return
        
        viewModelScope.launch {
            gameClient.sendPlayerAction(playerId, PlayerActionType.SUBMIT_CLUE, clue)
        }
    }
    
    fun submitVote(votedPlayerId: String) {
        val playerId = _uiState.value.currentPlayerId ?: return
        
        viewModelScope.launch {
            gameClient.sendPlayerAction(playerId, PlayerActionType.SUBMIT_VOTE, votedPlayerId)
        }
    }
    
    fun submitChameleonGuess(guess: String) {
        val playerId = _uiState.value.currentPlayerId ?: return
        
        viewModelScope.launch {
            gameClient.sendPlayerAction(playerId, PlayerActionType.SUBMIT_CHAMELEON_GUESS, guess)
        }
    }
    
    fun startVoting() {
        val playerId = _uiState.value.currentPlayerId ?: return
        
        viewModelScope.launch {
            gameClient.sendPlayerAction(playerId, PlayerActionType.START_VOTING)
        }
    }
    
    fun disconnect() {
        viewModelScope.launch {
            gameClient.disconnect()
            _uiState.value = NetworkGameUiState()
        }
    }
    
    fun getSecretWordForPlayer(playerId: String): String? {
        return if (_uiState.value.gameState.chameleonPlayerId == playerId) {
            null
        } else {
            _uiState.value.gameState.secretWord
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}
