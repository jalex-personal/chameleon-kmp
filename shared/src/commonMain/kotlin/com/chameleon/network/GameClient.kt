package com.chameleon.network

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.json.Json

class GameClient {
    private val client = HttpClient {
        install(WebSockets)
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }
    
    private var session: DefaultClientWebSocketSession? = null
    private var isConnected = false
    
    private val _messageFlow = MutableSharedFlow<NetworkMessage>()
    val messageFlow: SharedFlow<NetworkMessage> = _messageFlow
    
    private val _connectionStateFlow = MutableSharedFlow<Boolean>(replay = 1)
    val connectionStateFlow: SharedFlow<Boolean> = _connectionStateFlow
    
    suspend fun connect(hostIp: String, port: Int = 8080, playerName: String): Boolean {
        if (isConnected) return true
        
        try {
            session = client.webSocketSession(
                host = hostIp,
                port = port,
                path = "/game"
            )
            
            isConnected = true
            _connectionStateFlow.emit(true)
            
            // Send join request
            sendMessage(NetworkMessage.JoinGameRequest(playerName))
            
            // Start listening for messages
            listenForMessages()
            
            return true
        } catch (e: Exception) {
            isConnected = false
            _connectionStateFlow.emit(false)
            return false
        }
    }
    
    suspend fun disconnect() {
        session?.close()
        session = null
        isConnected = false
        _connectionStateFlow.emit(false)
    }
    
    suspend fun sendPlayerAction(playerId: String, action: PlayerActionType, data: String = "") {
        sendMessage(NetworkMessage.PlayerAction(playerId, action, data))
    }
    
    private suspend fun sendMessage(message: NetworkMessage) {
        if (!isConnected || session == null) return
        
        try {
            val messageText = Json.encodeToString(NetworkMessage.serializer(), message)
            session?.send(Frame.Text(messageText))
        } catch (e: Exception) {
            // Handle send failure
            isConnected = false
            _connectionStateFlow.emit(false)
        }
    }
    
    private suspend fun listenForMessages() {
        val currentSession = session ?: return
        
        try {
            for (frame in currentSession.incoming) {
                if (frame is Frame.Text) {
                    val message = Json.decodeFromString<NetworkMessage>(frame.readText())
                    _messageFlow.emit(message)
                }
            }
        } catch (e: ClosedReceiveChannelException) {
            // Connection closed
        } catch (e: Exception) {
            // Handle other exceptions
        } finally {
            isConnected = false
            _connectionStateFlow.emit(false)
        }
    }
    
    fun isConnected(): Boolean = isConnected
}
