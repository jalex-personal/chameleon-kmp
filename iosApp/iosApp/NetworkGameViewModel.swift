import Foundation
import Combine
import shared

@MainActor
class NetworkGameViewModel: ObservableObject {
    @Published var gameState = GameState()
    @Published var isConnected = false
    @Published var connectionError: String?
    @Published var backendIp = "192.168.1.100"
    @Published var backendPort = 8080
    @Published var currentPlayerId: String?
    @Published var isLoading = false
    
    private let gameClient = GameClient()
    private var cancellables = Set<AnyCancellable>()
    
    init() {
        observeNetworkMessages()
        observeConnectionState()
    }
    
    private func observeNetworkMessages() {
        Task {
            for await message in gameClient.messageFlow {
                await handleNetworkMessage(message)
            }
        }
    }
    
    private func observeConnectionState() {
        Task {
            for await connected in gameClient.connectionStateFlow {
                await MainActor.run {
                    self.isConnected = connected
                    if !connected {
                        self.connectionError = "Connection lost"
                    }
                }
            }
        }
    }
    
    private func handleNetworkMessage(_ message: NetworkMessage) async {
        switch message {
        case let joinResponse as NetworkMessage.JoinGameResponse:
            if joinResponse.success {
                currentPlayerId = joinResponse.playerId
                connectionError = nil
                isLoading = false
            } else {
                connectionError = joinResponse.message
                isLoading = false
            }
        case let gameStateUpdate as NetworkMessage.GameStateUpdate:
            gameState = gameStateUpdate.gameState
        case let error as NetworkMessage.Error:
            connectionError = error.message
            isLoading = false
        default:
            break
        }
    }
    
    func createGame(playerName: String) {
        isLoading = true
        connectionError = nil
        
        Task {
            let success = try await gameClient.createGame(hostIp: backendIp, port: Int32(backendPort), playerName: playerName)
            await MainActor.run {
                if !success {
                    self.connectionError = "Failed to connect to backend server"
                    self.isLoading = false
                }
            }
        }
    }
    
    func setBackendAddress(ip: String, port: Int) {
        backendIp = ip
        backendPort = port
    }
    
    func joinGame(playerName: String) {
        isLoading = true
        connectionError = nil
        
        Task {
            let success = try await gameClient.connect(hostIp: backendIp, port: Int32(backendPort), playerName: playerName)
            await MainActor.run {
                if !success {
                    self.connectionError = "Failed to connect to backend server"
                    self.isLoading = false
                }
            }
        }
    }
    
    func startGame() {
        guard let playerId = currentPlayerId else { return }
        
        Task {
            try await gameClient.sendPlayerAction(playerId: playerId, action: PlayerActionType.readyForNextRound, data: "")
        }
    }
    
    func submitClue(_ clue: String) {
        guard let playerId = currentPlayerId else { return }
        
        Task {
            try await gameClient.sendPlayerAction(playerId: playerId, action: PlayerActionType.submitClue, data: clue)
        }
    }
    
    func submitVote(votedPlayerId: String) {
        guard let playerId = currentPlayerId else { return }
        
        Task {
            try await gameClient.sendPlayerAction(playerId: playerId, action: PlayerActionType.submitVote, data: votedPlayerId)
        }
    }
    
    func submitChameleonGuess(_ guess: String) {
        guard let playerId = currentPlayerId else { return }
        
        Task {
            try await gameClient.sendPlayerAction(playerId: playerId, action: PlayerActionType.submitChameleonGuess, data: guess)
        }
    }
    
    func startVoting() {
        guard let playerId = currentPlayerId else { return }
        
        Task {
            try await gameClient.sendPlayerAction(playerId: playerId, action: PlayerActionType.startVoting, data: "")
        }
    }
    
    func disconnect() {
        Task {
            try await gameClient.disconnect()
            await MainActor.run {
                self.gameState = GameState()
                self.isConnected = false
                self.connectionError = nil
                self.currentPlayerId = nil
                self.isLoading = false
            }
        }
    }
    
    func getSecretWordForPlayer(playerId: String) -> String? {
        return gameState.chameleonPlayerId == playerId ? nil : gameState.secretWord
    }
}
