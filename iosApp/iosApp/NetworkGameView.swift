import SwiftUI
import shared

struct NetworkGameView: View {
    @ObservedObject var viewModel: NetworkGameViewModel
    let onBackToSetup: () -> Void
    
    var body: some View {
        VStack(spacing: 0) {
            if !viewModel.isConnected {
                Text("Connection lost. Trying to reconnect...")
                    .foregroundColor(.red)
                    .padding(16)
                    .background(Color(.systemRed).opacity(0.1))
                    .cornerRadius(12)
                    .padding(.horizontal, 16)
                    .padding(.top, 8)
            }
            
            ScrollView {
                LazyVStack(spacing: 16) {
                    GameInfoCard(viewModel: viewModel)
                    
                    PlayersCard(viewModel: viewModel)
                    
                    switch viewModel.gameState.phase {
                    case GamePhase.givingClues:
                        ClueGivingCard(viewModel: viewModel)
                    case GamePhase.discussion:
                        DiscussionCard(viewModel: viewModel)
                    case GamePhase.voting:
                        VotingCard(viewModel: viewModel)
                    case GamePhase.chameleonGuess:
                        ChameleonGuessCard(viewModel: viewModel)
                    case GamePhase.roundEnd:
                        RoundEndCard(viewModel: viewModel)
                    case GamePhase.gameEnd:
                        GameEndCard(viewModel: viewModel)
                    default:
                        EmptyView()
                    }
                }
                .padding(16)
            }
        }
        .navigationTitle("The Chameleon - Round \(viewModel.gameState.currentRound)")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button("Back") {
                    onBackToSetup()
                }
            }
        }
    }
}

struct GameInfoCard: View {
    @ObservedObject var viewModel: NetworkGameViewModel
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(viewModel.gameState.topicCard?.title ?? "Loading...")
                .font(.title2)
                .fontWeight(.bold)
            
            let secretWord = viewModel.currentPlayerId.flatMap { playerId in
                viewModel.getSecretWordForPlayer(playerId: playerId)
            }
            
            if let secretWord = secretWord {
                Text("Secret Word: \(secretWord)")
                    .font(.title3)
                    .fontWeight(.bold)
                    .foregroundColor(.blue)
            } else {
                Text("You are the CHAMELEON! 🦎")
                    .font(.title3)
                    .fontWeight(.bold)
                    .foregroundColor(.red)
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(radius: 4)
    }
}

struct PlayersCard: View {
    @ObservedObject var viewModel: NetworkGameViewModel
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Players")
                .font(.title3)
                .fontWeight(.bold)
            
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(viewModel.gameState.players, id: \.id) { player in
                        VStack(spacing: 4) {
                            Text(player.name)
                                .font(.body)
                                .fontWeight(player.id == viewModel.currentPlayerId ? .bold : .regular)
                            
                            Text("Score: \(player.score)")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        .padding(8)
                        .background(player.id == viewModel.currentPlayerId ? 
                                   Color(.systemBlue).opacity(0.1) : 
                                   Color(.secondarySystemBackground))
                        .cornerRadius(8)
                    }
                }
                .padding(.horizontal, 4)
            }
        }
        .padding(16)
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(radius: 4)
    }
}

struct ClueGivingCard: View {
    @ObservedObject var viewModel: NetworkGameViewModel
    @State private var clue = ""
    
    var body: some View {
        let currentPlayer = viewModel.gameState.currentPlayer
        let isMyTurn = currentPlayer?.id == viewModel.currentPlayerId
        
        VStack(alignment: .leading, spacing: 16) {
            Text(isMyTurn ? "Your turn to give a clue!" : "Waiting for \(currentPlayer?.name ?? "player")")
                .font(.title3)
                .fontWeight(.bold)
            
            if isMyTurn {
                TextField("Your clue", text: $clue)
                    .textFieldStyle(.roundedBorder)
                
                Button(action: {
                    viewModel.submitClue(clue)
                    clue = ""
                }) {
                    Text("Submit Clue")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
                .disabled(clue.trimmingCharacters(in: .whitespaces).isEmpty)
            }
            
            if !viewModel.gameState.clues.isEmpty {
                Text("Clues given:")
                    .font(.body)
                    .fontWeight(.bold)
                
                ForEach(Array(viewModel.gameState.clues.keys), id: \.self) { playerId in
                    if let playerClue = viewModel.gameState.clues[playerId],
                       let player = viewModel.gameState.players.first(where: { $0.id == playerId }) {
                        Text("\(player.name): \(playerClue)")
                            .font(.body)
                    }
                }
            }
        }
        .padding(16)
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(radius: 4)
    }
}

struct DiscussionCard: View {
    @ObservedObject var viewModel: NetworkGameViewModel
    
    var body: some View {
        VStack(spacing: 16) {
            Text("Discussion Time")
                .font(.title3)
                .fontWeight(.bold)
            
            Text("Discuss the clues and try to identify the Chameleon!")
                .font(.body)
                .multilineTextAlignment(.center)
            
            Button(action: {
                viewModel.startVoting()
            }) {
                Text("Start Voting")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
        }
        .padding(16)
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(radius: 4)
    }
}

struct VotingCard: View {
    @ObservedObject var viewModel: NetworkGameViewModel
    
    var body: some View {
        let hasVoted = viewModel.currentPlayerId.map { viewModel.gameState.votes.keys.contains($0) } ?? false
        
        VStack(alignment: .leading, spacing: 16) {
            Text("Vote for the Chameleon")
                .font(.title3)
                .fontWeight(.bold)
            
            if hasVoted {
                Text("You have voted. Waiting for other players...")
                    .font(.body)
                    .frame(maxWidth: .infinity, alignment: .center)
            } else {
                ForEach(viewModel.gameState.players.filter { $0.id != viewModel.currentPlayerId }, id: \.id) { player in
                    Button(action: {
                        viewModel.submitVote(votedPlayerId: player.id)
                    }) {
                        Text(player.name)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(16)
                    }
                    .buttonStyle(.bordered)
                }
            }
            
            if !viewModel.gameState.votes.isEmpty {
                Text("Votes cast: \(viewModel.gameState.votes.count)/\(viewModel.gameState.players.count)")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .padding(16)
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(radius: 4)
    }
}

struct ChameleonGuessCard: View {
    @ObservedObject var viewModel: NetworkGameViewModel
    @State private var guess = ""
    
    var body: some View {
        let isChameleon = viewModel.gameState.chameleonPlayerId == viewModel.currentPlayerId
        
        VStack(alignment: .leading, spacing: 16) {
            if isChameleon {
                Text("You are the Chameleon! Guess the secret word:")
                    .font(.title3)
                    .fontWeight(.bold)
                
                TextField("Your guess", text: $guess)
                    .textFieldStyle(.roundedBorder)
                
                Button(action: {
                    viewModel.submitChameleonGuess(guess)
                    guess = ""
                }) {
                    Text("Submit Guess")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
                .disabled(guess.trimmingCharacters(in: .whitespaces).isEmpty)
            } else {
                Text("Waiting for the Chameleon to guess...")
                    .font(.title3)
                    .fontWeight(.bold)
                    .frame(maxWidth: .infinity, alignment: .center)
            }
        }
        .padding(16)
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(radius: 4)
    }
}

struct RoundEndCard: View {
    @ObservedObject var viewModel: NetworkGameViewModel
    
    var body: some View {
        VStack(spacing: 16) {
            Text("Round \(viewModel.gameState.currentRound) Complete!")
                .font(.title3)
                .fontWeight(.bold)
            
            Text("The Chameleon was: \(viewModel.gameState.chameleonPlayer?.name ?? "Unknown")")
                .font(.body)
            
            Button(action: {
                
            }) {
                Text("Next Round")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
        }
        .padding(16)
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(radius: 4)
    }
}

struct GameEndCard: View {
    @ObservedObject var viewModel: NetworkGameViewModel
    
    var body: some View {
        let winner = viewModel.gameState.players.max(by: { $0.score < $1.score })
        
        VStack(spacing: 16) {
            Text("Game Over!")
                .font(.title2)
                .fontWeight(.bold)
            
            Text("Winner: \(winner?.name ?? "Unknown")")
                .font(.title3)
                .fontWeight(.bold)
            
            Text("Final Scores:")
                .font(.body)
                .fontWeight(.bold)
            
            ForEach(viewModel.gameState.players.sorted(by: { $0.score > $1.score }), id: \.id) { player in
                Text("\(player.name): \(player.score)")
                    .font(.body)
            }
        }
        .padding(16)
        .background(Color(.systemBlue).opacity(0.1))
        .cornerRadius(12)
        .shadow(radius: 4)
    }
}

#Preview {
    NavigationStack {
        NetworkGameView(
            viewModel: NetworkGameViewModel(),
            onBackToSetup: {}
        )
    }
}
