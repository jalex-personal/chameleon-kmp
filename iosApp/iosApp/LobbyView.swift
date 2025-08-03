import SwiftUI
import shared

struct LobbyView: View {
    @ObservedObject var viewModel: NetworkGameViewModel
    let onBackToSetup: () -> Void
    let onGameStarted: () -> Void
    
    var body: some View {
        VStack(spacing: 16) {
            if !viewModel.isConnected {
                VStack(spacing: 8) {
                    Text("Connection Lost")
                        .font(.title3)
                        .foregroundColor(.red)
                    
                    Text("Trying to reconnect...")
                        .font(.body)
                        .foregroundColor(.secondary)
                }
                .padding(16)
                .background(Color(.systemRed).opacity(0.1))
                .cornerRadius(12)
            }
            
            VStack(alignment: .leading, spacing: 16) {
                Text("Players in Game (\(viewModel.gameState.players.count))")
                    .font(.title2)
                    .fontWeight(.bold)
                
                if viewModel.gameState.players.isEmpty {
                    Text("Waiting for players...")
                        .foregroundColor(.secondary)
                        .frame(maxWidth: .infinity, alignment: .center)
                } else {
                    ScrollView {
                        LazyVStack(spacing: 8) {
                            ForEach(viewModel.gameState.players, id: \.id) { player in
                                HStack {
                                    Text(player.name + (player.id == viewModel.currentPlayerId ? " (You)" : ""))
                                        .font(.body)
                                        .fontWeight(player.id == viewModel.currentPlayerId ? .bold : .regular)
                                    
                                    Spacer()
                                    
                                    Text("Score: \(player.score)")
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                }
                                .padding(12)
                                .background(player.id == viewModel.currentPlayerId ? 
                                           Color(.systemBlue).opacity(0.1) : 
                                           Color(.secondarySystemBackground))
                                .cornerRadius(8)
                            }
                        }
                    }
                    .frame(maxHeight: 300)
                }
            }
            .padding(16)
            .background(Color(.systemBackground))
            .cornerRadius(12)
            .shadow(radius: 4)
            
            Text("Waiting for host to start the game...")
                .font(.body)
                .foregroundColor(.secondary)
                .padding(16)
                .background(Color(.secondarySystemBackground))
                .cornerRadius(12)
            
            Spacer()
        }
        .padding(16)
        .navigationTitle("Game Lobby")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button("Back") {
                    onBackToSetup()
                }
            }
        }
        .onChange(of: viewModel.gameState.phase) { phase in
            if phase != GamePhase.setup {
                onGameStarted()
            }
        }
    }
}

#Preview {
    NavigationStack {
        LobbyView(
            viewModel: NetworkGameViewModel(),
            onBackToSetup: {},
            onGameStarted: {}
        )
    }
}
