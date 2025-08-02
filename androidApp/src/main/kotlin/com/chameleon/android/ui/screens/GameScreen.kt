package com.chameleon.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chameleon.android.ui.viewmodel.GameViewModel
import com.chameleon.game.GamePhase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    onBackToSetup: () -> Unit,
    viewModel: GameViewModel = viewModel()
) {
    val gameState by viewModel.gameState.collectAsState()
    
    LaunchedEffect(Unit) {
        if (gameState.players.isEmpty()) {
            viewModel.startNewGame(listOf("Player 1", "Player 2", "Player 3", "Player 4"))
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("The Chameleon - Round ${gameState.currentRound}") },
            navigationIcon = {
                IconButton(onClick = onBackToSetup) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ScoreBoard(gameState = gameState)
            }
            
            item {
                when (gameState.phase) {
                    GamePhase.SETUP -> {
                        SetupPhaseCard(onStartRound = { viewModel.startNewRound() })
                    }
                    GamePhase.GIVING_CLUES -> {
                        GivingCluesPhase(
                            gameState = gameState,
                            onSubmitClue = { playerId, clue -> viewModel.submitClue(playerId, clue) }
                        )
                    }
                    GamePhase.DISCUSSION -> {
                        DiscussionPhase(
                            gameState = gameState,
                            onStartVoting = { viewModel.startVoting() }
                        )
                    }
                    GamePhase.VOTING -> {
                        VotingPhase(
                            gameState = gameState,
                            onSubmitVote = { voterPlayerId, votedPlayerId -> 
                                viewModel.submitVote(voterPlayerId, votedPlayerId) 
                            }
                        )
                    }
                    GamePhase.CHAMELEON_GUESS -> {
                        ChameleonGuessPhase(
                            gameState = gameState,
                            onSubmitGuess = { guess -> viewModel.submitChameleonGuess(guess) }
                        )
                    }
                    GamePhase.ROUND_END -> {
                        RoundEndPhase(
                            gameState = gameState,
                            onNextRound = { viewModel.startNewRound() }
                        )
                    }
                    GamePhase.GAME_END -> {
                        GameEndPhase(
                            gameState = gameState,
                            onNewGame = { viewModel.startNewGame(gameState.players.map { it.name }) }
                        )
                    }
                }
            }
            
            if (gameState.topicCard != null) {
                item {
                    TopicCardDisplay(
                        topicCard = gameState.topicCard!!,
                        secretWord = gameState.secretWord
                    )
                }
            }
        }
    }
}

@Composable
fun ScoreBoard(gameState: com.chameleon.game.GameState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Scoreboard",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(gameState.players) { player ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (player.isChameleon) 
                                MaterialTheme.colorScheme.errorContainer 
                            else 
                                MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = player.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${player.score}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SetupPhaseCard(onStartRound: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ready to start?",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(
                onClick = onStartRound,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Round")
            }
        }
    }
}

@Composable
fun GivingCluesPhase(
    gameState: com.chameleon.game.GameState,
    onSubmitClue: (String, String) -> Unit
) {
    var currentClue by remember { mutableStateOf("") }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Giving Clues Phase",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            gameState.currentPlayer?.let { player ->
                Text(
                    text = "${player.name}'s turn",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = currentClue,
                    onValueChange = { currentClue = it },
                    label = { Text("Your clue") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        if (currentClue.isNotBlank()) {
                            onSubmitClue(player.id, currentClue)
                            currentClue = ""
                        }
                    },
                    enabled = currentClue.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Submit Clue")
                }
            }
            
            if (gameState.clues.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Clues given:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                gameState.clues.forEach { (playerId, clue) ->
                    val playerName = gameState.players.find { it.id == playerId }?.name ?: "Unknown"
                    Text(
                        text = "$playerName: $clue",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DiscussionPhase(
    gameState: com.chameleon.game.GameState,
    onStartVoting: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Discussion Phase",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Discuss the clues and try to identify the Chameleon!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "All clues:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            gameState.clues.forEach { (playerId, clue) ->
                val playerName = gameState.players.find { it.id == playerId }?.name ?: "Unknown"
                Text(
                    text = "$playerName: $clue",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onStartVoting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Voting")
            }
        }
    }
}

@Composable
fun VotingPhase(
    gameState: com.chameleon.game.GameState,
    onSubmitVote: (String, String) -> Unit
) {
    var selectedPlayerId by remember { mutableStateOf<String?>(null) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Voting Phase",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Who do you think is the Chameleon?",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            gameState.players.forEach { player ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedPlayerId == player.id,
                        onClick = { selectedPlayerId = player.id }
                    )
                    Text(
                        text = player.name,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    selectedPlayerId?.let { votedPlayerId ->
                        // For simplicity, we'll vote as the first player
                        onSubmitVote(gameState.players.first().id, votedPlayerId)
                    }
                },
                enabled = selectedPlayerId != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit Vote")
            }
            
            if (gameState.votes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Votes: ${gameState.votes.size}/${gameState.players.size}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun ChameleonGuessPhase(
    gameState: com.chameleon.game.GameState,
    onSubmitGuess: (String) -> Unit
) {
    var guess by remember { mutableStateOf("") }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Chameleon's Last Chance!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "${gameState.chameleonPlayer?.name} was identified as the Chameleon! " +
                        "Guess the secret word to win 2 points!",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            OutlinedTextField(
                value = guess,
                onValueChange = { guess = it },
                label = { Text("Your guess") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = {
                    if (guess.isNotBlank()) {
                        onSubmitGuess(guess)
                        guess = ""
                    }
                },
                enabled = guess.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit Guess")
            }
        }
    }
}

@Composable
fun RoundEndPhase(
    gameState: com.chameleon.game.GameState,
    onNextRound: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Round ${gameState.currentRound - 1} Complete!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "The secret word was: ${gameState.secretWord}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            gameState.chameleonPlayer?.let { chameleon ->
                Text(
                    text = "${chameleon.name} was the Chameleon",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            Button(
                onClick = onNextRound,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Next Round")
            }
        }
    }
}

@Composable
fun GameEndPhase(
    gameState: com.chameleon.game.GameState,
    onNewGame: () -> Unit
) {
    val winner = gameState.players.maxByOrNull { it.score }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Game Over!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            winner?.let {
                Text(
                    text = "${it.name} wins with ${it.score} points!",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            Button(
                onClick = onNewGame,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("New Game")
            }
        }
    }
}

@Composable
fun TopicCardDisplay(
    topicCard: com.chameleon.game.TopicCard,
    secretWord: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Topic: ${topicCard.title}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                topicCard.words.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        row.forEach { word ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        color = if (word == secretWord) 
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                        else 
                                            MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = word,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
