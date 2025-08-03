import SwiftUI

struct ContentView: View {
    @StateObject private var viewModel = NetworkGameViewModel()
    @State private var currentScreen: Screen = .networkSetup
    
    enum Screen {
        case networkSetup
        case hostGame
        case joinGame
        case lobby
        case networkGame
        case localSetup
        case localGame
    }
    
    var body: some View {
        NavigationStack {
            switch currentScreen {
            case .networkSetup:
                NetworkSetupView(
                    onHostGame: { currentScreen = .hostGame },
                    onJoinGame: { currentScreen = .joinGame },
                    onLocalGame: { currentScreen = .localSetup }
                )
            case .hostGame:
                HostGameView(
                    viewModel: viewModel,
                    onBackToSetup: { 
                        viewModel.disconnect()
                        currentScreen = .networkSetup 
                    },
                    onStartGame: { currentScreen = .networkGame }
                )
            case .joinGame:
                JoinGameView(
                    viewModel: viewModel,
                    onBackToSetup: { 
                        viewModel.disconnect()
                        currentScreen = .networkSetup 
                    },
                    onJoinedGame: { currentScreen = .lobby }
                )
            case .lobby:
                LobbyView(
                    viewModel: viewModel,
                    onBackToSetup: { 
                        viewModel.disconnect()
                        currentScreen = .networkSetup 
                    },
                    onGameStarted: { currentScreen = .networkGame }
                )
            case .networkGame:
                NetworkGameView(
                    viewModel: viewModel,
                    onBackToSetup: { 
                        viewModel.disconnect()
                        currentScreen = .networkSetup 
                    }
                )
            case .localSetup:
                Text("Local Setup - Not implemented")
                    .navigationTitle("Local Setup")
                    .navigationBarTitleDisplayMode(.inline)
                    .toolbar {
                        ToolbarItem(placement: .navigationBarLeading) {
                            Button("Back") {
                                currentScreen = .networkSetup
                            }
                        }
                    }
            case .localGame:
                Text("Local Game - Not implemented")
                    .navigationTitle("Local Game")
                    .navigationBarTitleDisplayMode(.inline)
                    .toolbar {
                        ToolbarItem(placement: .navigationBarLeading) {
                            Button("Back") {
                                currentScreen = .networkSetup
                            }
                        }
                    }
            }
        }
    }
}

#Preview {
    ContentView()
}
