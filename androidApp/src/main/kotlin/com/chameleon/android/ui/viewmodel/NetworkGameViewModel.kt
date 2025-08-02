package com.chameleon.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chameleon.game.GameState
import com.chameleon.network.GameClient
import com.chameleon.network.GameServer
import com.chameleon.network.NetworkMessage
import com.chameleon.network.PlayerActionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NetworkGameUiState(
    val gameState: GameState = GameState(),
    val isHost: Boolean = false,
    val isConnected: Boolean = false,
    val connectionError: String? = null,
    val hostIp: String? = null,
    val currentPlayerId: String? = null,
    val isLoading: Boolean = false
)

class NetworkGameViewModel : ViewModel() {
    private val gameServer = GameServer()
    private val gameClient = GameClient()
    
    private val _uiState = MutableStateFlow(NetworkGameUiState())
    val uiState: StateFlow<NetworkGameUiState> = _uiState.asStateFlow()
    
    init {
        observeNetworkMessages()
        observeConnectionState()
        observeGameState()
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
                    connectionError = if (!isConnected && !_uiState.value.isHost) "Connection lost" else null
                )
            }
        }
    }
    
    private fun observeGameState() {
        viewModelScope.launch {
            gameServer.gameStateFlow.collect { gameState ->
                if (_uiState.value.isHost) {
                    _uiState.value = _uiState.value.copy(
                        gameState = gameState
                    )
                }
            }
        }
    }
    
    fun startHosting(port: Int = 8080): Boolean {
        val hostIp = gameServer.startServer(port)
        return if (hostIp != null) {
            _uiState.value = _uiState.value.copy(
                isHost = true,
                isConnected = true,
                hostIp = hostIp,
                connectionError = null,
                currentPlayerId = "host_player"
            )
            
            viewModelScope.launch {
                gameClient.connect("localhost", port, "Host")
            }
            
            true
        } else {
            _uiState.value = _uiState.value.copy(
                connectionError = "Failed to start server"
            )
            false
        }
    }
    
    fun joinGame(hostIp: String, playerName: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            connectionError = null
        )
        
        viewModelScope.launch {
            val success = gameClient.connect(hostIp, 8080, playerName)
            if (!success) {
                _uiState.value = _uiState.value.copy(
                    connectionError = "Failed to connect to host",
                    isLoading = false
                )
            }
        }
    }
    
    fun startGame(playerNames: List<String>) {
        if (_uiState.value.isHost) {
            gameServer.startGame(playerNames)
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
            if (_uiState.value.isHost) {
                gameServer.stopServer()
            } else {
                gameClient.disconnect()
            }
            
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
