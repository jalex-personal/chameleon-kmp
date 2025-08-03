import SwiftUI

struct JoinGameView: View {
    @ObservedObject var viewModel: NetworkGameViewModel
    let onBackToSetup: () -> Void
    let onJoinedGame: () -> Void
    
    @State private var playerName = ""
    @State private var backendIp = ""
    @State private var backendPort = ""
    
    var body: some View {
        VStack(spacing: 24) {
            Spacer()
            
            VStack(spacing: 24) {
                Text("Join Multiplayer Game")
                    .font(.title2)
                    .fontWeight(.bold)
                
                TextField("Your Name", text: $playerName)
                    .textFieldStyle(.roundedBorder)
                
                TextField("Backend IP Address", text: $backendIp, prompt: Text("192.168.1.100"))
                    .textFieldStyle(.roundedBorder)
                    .keyboardType(.numbersAndPunctuation)
                
                TextField("Backend Port", text: $backendPort, prompt: Text("8080"))
                    .textFieldStyle(.roundedBorder)
                    .keyboardType(.numberPad)
                
                if let error = viewModel.connectionError {
                    Text(error)
                        .foregroundColor(.red)
                        .padding(12)
                        .background(Color(.systemRed).opacity(0.1))
                        .cornerRadius(8)
                }
                
                Button(action: {
                    viewModel.setBackendAddress(ip: backendIp.trimmingCharacters(in: .whitespaces), 
                                               port: Int(backendPort) ?? 8080)
                    viewModel.joinGame(playerName: playerName.trimmingCharacters(in: .whitespaces))
                }) {
                    HStack {
                        if viewModel.isLoading {
                            ProgressView()
                                .scaleEffect(0.8)
                                .tint(.white)
                        } else {
                            Text("Join Game")
                                .font(.title3)
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 56)
                }
                .buttonStyle(.borderedProminent)
                .disabled(backendIp.trimmingCharacters(in: .whitespaces).isEmpty || 
                         playerName.trimmingCharacters(in: .whitespaces).isEmpty || 
                         viewModel.isLoading)
            }
            .padding(24)
            .background(Color(.systemBackground))
            .cornerRadius(12)
            .shadow(radius: 4)
            
            Spacer()
            
            VStack(alignment: .leading, spacing: 8) {
                Text("How to Connect")
                    .font(.title3)
                    .fontWeight(.bold)
                
                Text("• Make sure the backend server is running\n• Get the backend server's IP address and port\n• Make sure you're on the same network as the backend\n• Enter your name and the backend details\n• Tap 'Join Game' to connect")
                    .font(.body)
                    .foregroundColor(.secondary)
            }
            .padding(16)
            .background(Color(.secondarySystemBackground))
            .cornerRadius(12)
            
            Spacer()
        }
        .padding(16)
        .navigationTitle("Join Game")
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
        .onChange(of: viewModel.isConnected) { connected in
            if connected && viewModel.currentPlayerId != nil {
                onJoinedGame()
            }
        }
    }
}
