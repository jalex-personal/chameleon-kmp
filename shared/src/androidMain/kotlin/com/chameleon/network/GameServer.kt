package com.chameleon.network

import com.chameleon.game.GameEngine
import com.chameleon.game.GameState
import com.chameleon.game.Player
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

actual class GameServer : GameServerInterface {
    private val gameEngine = GameEngine()
    private val connections = ConcurrentHashMap<String, DefaultWebSocketSession>()
    private val playerSessions = ConcurrentHashMap<String, String>() // sessionId to playerId
    
    private val _gameStateFlow = MutableSharedFlow<GameState>(replay = 1)
    override val gameStateFlow: SharedFlow<GameState> = _gameStateFlow
    
    private var server: NettyApplicationEngine? = null
    private var isHosting = false
    
    override fun startServer(port: Int): String? {
        if (isHosting) return null
        
        try {
            server = embeddedServer(Netty, port = port) {
                install(WebSockets) {
                    pingPeriodMillis = 15000
                    timeoutMillis = 15000
                    maxFrameSize = Long.MAX_VALUE
                    masking = false
                }
                
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                    })
                }
                
                routing {
                    webSocket("/game") {
                        handleWebSocketConnection(this)
                    }
                }
            }
            
            server?.start(wait = false)
            isHosting = true
            
            return getLocalIpAddress()
        } catch (e: Exception) {
            return null
        }
    }
    
    override fun stopServer() {
        server?.stop(1000, 2000)
        server = null
        isHosting = false
        connections.clear()
        playerSessions.clear()
    }
    
    private suspend fun handleWebSocketConnection(session: DefaultWebSocketSession) {
        val sessionId = generateSessionId()
        
        try {
            for (frame in session.incoming) {
                if (frame is Frame.Text) {
                    val message = Json.decodeFromString<NetworkMessage>(frame.readText())
                    handleMessage(sessionId, session, message)
                }
            }
        } catch (e: ClosedReceiveChannelException) {
            // Connection closed
        } catch (e: Exception) {
            // Handle other exceptions
        } finally {
            handlePlayerDisconnection(sessionId)
        }
    }
    
    private suspend fun handleMessage(sessionId: String, session: DefaultWebSocketSession, message: NetworkMessage) {
        when (message) {
            is NetworkMessage.JoinGameRequest -> {
                handleJoinGame(sessionId, session, message.playerName)
            }
            is NetworkMessage.PlayerAction -> {
                handlePlayerAction(message)
            }
            else -> {
                // Handle other message types
            }
        }
    }
    
    private suspend fun handleJoinGame(sessionId: String, session: DefaultWebSocketSession, playerName: String) {
        val currentPlayers = gameEngine.gameState.players
        
        if (currentPlayers.size >= 8) {
            session.send(Frame.Text(Json.encodeToString(NetworkMessage.serializer(), 
                NetworkMessage.JoinGameResponse(false, null, "Game is full"))))
            return
        }
        
        val playerId = "player_${currentPlayers.size}"
        val newPlayer = Player(id = playerId, name = playerName)
        
        connections[sessionId] = session
        playerSessions[sessionId] = playerId
        
        // Add player to game
        val updatedPlayers = currentPlayers + newPlayer
        val newGameState = gameEngine.gameState.copy(players = updatedPlayers)
        
        // Send success response
        session.send(Frame.Text(Json.encodeToString(NetworkMessage.serializer(),
            NetworkMessage.JoinGameResponse(true, playerId, "Joined successfully"))))
        
        // Broadcast updated game state
        broadcastGameState(newGameState)
    }
    
    private suspend fun handlePlayerAction(action: NetworkMessage.PlayerAction) {
        when (action.action) {
            PlayerActionType.SUBMIT_CLUE -> {
                gameEngine.submitClue(action.playerId, action.data)
            }
            PlayerActionType.SUBMIT_VOTE -> {
                gameEngine.submitVote(action.playerId, action.data)
            }
            PlayerActionType.SUBMIT_CHAMELEON_GUESS -> {
                gameEngine.submitChameleonGuess(action.data)
            }
            PlayerActionType.START_VOTING -> {
                gameEngine.startVoting()
            }
            PlayerActionType.READY_FOR_NEXT_ROUND -> {
                gameEngine.startNewRound()
            }
        }
        
        broadcastGameState(gameEngine.gameState)
    }
    
    private suspend fun handlePlayerDisconnection(sessionId: String) {
        val playerId = playerSessions[sessionId]
        if (playerId != null) {
            connections.remove(sessionId)
            playerSessions.remove(sessionId)
            
            // Remove player from game
            val updatedPlayers = gameEngine.gameState.players.filter { it.id != playerId }
            val newGameState = gameEngine.gameState.copy(players = updatedPlayers)
            
            // Broadcast player left message
            broadcastMessage(NetworkMessage.PlayerLeft(playerId, 
                gameEngine.gameState.players.find { it.id == playerId }?.name ?: "Unknown"))
            
            broadcastGameState(newGameState)
        }
    }
    
    private suspend fun broadcastGameState(gameState: GameState) {
        _gameStateFlow.emit(gameState)
        broadcastMessage(NetworkMessage.GameStateUpdate(gameState))
    }
    
    private suspend fun broadcastMessage(message: NetworkMessage) {
        val messageText = Json.encodeToString(NetworkMessage.serializer(), message)
        connections.values.forEach { session ->
            try {
                session.send(Frame.Text(messageText))
            } catch (e: Exception) {
                // Handle send failure
            }
        }
    }
    
    override fun startGame(playerNames: List<String>) {
        val gameState = gameEngine.startNewGame(playerNames)
        _gameStateFlow.tryEmit(gameState)
    }
    
    private fun generateSessionId(): String = "session_${System.currentTimeMillis()}_${(0..999).random()}"
    
    private fun getLocalIpAddress(): String {
        return "192.168.1.100"
    }
}
