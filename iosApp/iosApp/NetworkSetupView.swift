import SwiftUI

struct NetworkSetupView: View {
    let onHostGame: () -> Void
    let onJoinGame: () -> Void
    let onLocalGame: () -> Void
    
    var body: some View {
        VStack(spacing: 20) {
            Spacer()
            
            Text("The Chameleon")
                .font(.largeTitle)
                .fontWeight(.bold)
                .padding(.bottom, 48)
            
            VStack(spacing: 16) {
                VStack(spacing: 24) {
                    Text("Choose Game Mode")
                        .font(.title2)
                        .padding(.bottom, 8)
                    
                    Button(action: onHostGame) {
                        Text("Host Multiplayer Game")
                            .font(.title3)
                            .frame(maxWidth: .infinity)
                            .frame(height: 56)
                    }
                    .buttonStyle(.borderedProminent)
                    
                    Button(action: onJoinGame) {
                        Text("Join Multiplayer Game")
                            .font(.title3)
                            .frame(maxWidth: .infinity)
                            .frame(height: 56)
                    }
                    .buttonStyle(.borderedProminent)
                    
                    Button(action: onLocalGame) {
                        Text("Local Game (Single Device)")
                            .font(.title3)
                            .frame(maxWidth: .infinity)
                            .frame(height: 56)
                    }
                    .buttonStyle(.bordered)
                }
                .padding(24)
                .background(Color(.systemBackground))
                .cornerRadius(12)
                .shadow(radius: 4)
            }
            
            Spacer()
            
            VStack(alignment: .leading, spacing: 8) {
                Text("Multiplayer Info")
                    .font(.title3)
                    .fontWeight(.bold)
                
                Text("• Host: Start a game server for others to join\n• Join: Connect to a friend's hosted game\n• Local: Play on one device (original mode)\n• All players must be on the same WiFi network")
                    .font(.body)
                    .foregroundColor(.secondary)
            }
            .padding(16)
            .background(Color(.secondarySystemBackground))
            .cornerRadius(12)
            
            Spacer()
        }
        .padding(16)
        .navigationTitle("The Chameleon")
        .navigationBarTitleDisplayMode(.inline)
    }
}

#Preview {
    NavigationStack {
        NetworkSetupView(
            onHostGame: {},
            onJoinGame: {},
            onLocalGame: {}
        )
    }
}
