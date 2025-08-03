import SwiftUI

struct HostGameView: View {
    @ObservedObject var viewModel: NetworkGameViewModel
    let onBackToSetup: () -> Void
    let onStartGame: () -> Void
    
    @State private var playerName = ""
    @State private var backendIp = ""
    @State private var backendPort = ""
    
    var body: some View {
        VStack(spacing: 16) {
            if !viewModel.isConnected {
                VStack(spacing: 16) {
                    Text("Backend Server Configuration")
                        .font(.title2)
                        .fontWeight(.bold)
                        .padding(.bottom, 16)
                    
                    TextField("Backend IP Address", text: $backendIp)
                        .textFieldStyle(.roundedBorder)
                    
                    TextField("Backend Port", text: $backendPort)
                        .textFieldStyle(.roundedBorder)
                        .keyboardType(.numberPad)
                    
                    TextField("Your Name", text: $playerName)
                        .textFieldStyle(.roundedBorder)
                    
                    Button(action: {
                        viewModel.setBackendAddress(ip: backendIp, port: Int(backendPort) ?? 8080)
                        viewModel.createGame(playerName: playerName)
                    }) {
                        HStack {
                            if viewModel.isLoading {
                                ProgressView()
                                    .scaleEffect(0.8)
                            } else {
                                Text("Create Game")
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 44)
                    }
                    .buttonStyle(.borderedProminent)
                    .disabled(playerName.isEmpty || backendIp.isEmpty || viewModel.isLoading)
                }
                .padding(16)
                .background(Color(.systemBackground))
                .cornerRadius(12)
                .shadow(radius: 4)
                
                if let error = viewModel.connectionError {
                    Text(error)
                        .foregroundColor(.red)
                        .padding(16)
                        .background(Color(.systemRed).opacity(0.1))
                        .cornerRadius(12)
                }
            } else {
                VStack(spacing: 16) {
                    Text("Connected to Backend")
                        .font(.title2)
                        .fontWeight(.bold)
                    
                    Text("Backend: \(viewModel.backendIp):\(viewModel.backendPort)")
                        .font(.body)
                        .foregroundColor(.secondary)
                }
                .padding(16)
                .background(Color(.systemBackground))
                .cornerRadius(12)
                .shadow(radius: 4)
            }
            
            VStack(alignment: .leading, spacing: 16) {
                Text("Connected Players (\(viewModel.gameState.players.count))")
                    .font(.title2)
                    .fontWeight(.bold)
                
                if viewModel.gameState.players.isEmpty {
                    Text("Waiting for players to join...")
                        .foregroundColor(.secondary)
                        .frame(maxWidth: .infinity, alignment: .center)
                } else {
                    ScrollView {
                        LazyVStack(spacing: 8) {
                            ForEach(viewModel.gameState.players, id: \.id) { player in
                                Text(player.name)
                                    .padding(12)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                    .background(Color(.secondarySystemBackground))
                                    .cornerRadius(8)
                            }
                        }
                    }
                    .frame(maxHeight: 200)
                }
            }
            .padding(16)
            .background(Color(.systemBackground))
            .cornerRadius(12)
            .shadow(radius: 4)
            
            if viewModel.isConnected {
                Button(action: {
                    viewModel.startGame()
                    onStartGame()
                }) {
                    Text(viewModel.gameState.players.count < 3 ? "Need at least 3 players" : "Start Game")
                        .font(.title3)
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                }
                .buttonStyle(.borderedProminent)
                .disabled(viewModel.gameState.players.count < 3)
            }
            
            Spacer()
        }
        .padding(16)
        .navigationTitle("Create Game")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button("Back") {
                    onBackToSetup()
                }
            }
        }
        .onAppear {
            backendIp = viewModel.backendIp
            backendPort = String(viewModel.backendPort)
        }
    }
}

#Preview {
    NavigationStack {
        HostGameView(
            viewModel: NetworkGameViewModel(),
            onBackToSetup: {},
            onStartGame: {}
        )
    }
}
