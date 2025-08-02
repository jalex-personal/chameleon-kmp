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
import com.chameleon.android.ui.viewmodel.NetworkGameViewModel
import com.chameleon.game.GamePhase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkGameScreen(
    onBackToSetup: () -> Unit,
    viewModel: NetworkGameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val gameState = uiState.gameState
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("The Chameleon - Round ${gameState.currentRound}") },
            navigationIcon = {
                IconButton(onClick = {
                    viewModel.disconnect()
                    onBackToSetup()
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )
        
        if (!uiState.isConnected && !uiState.isHost) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "Connection lost. Trying to reconnect...",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GameInfoCard(gameState, uiState, viewModel)
            }
            
            item {
                PlayersCard(gameState, uiState)
            }
            
            when (gameState.phase) {
                GamePhase.GIVING_CLUES -> {
                    item {
                        ClueGivingCard(gameState, uiState, viewModel)
                    }
                }
                GamePhase.DISCUSSION -> {
                    item {
                        DiscussionCard(gameState, viewModel)
                    }
                }
                GamePhase.VOTING -> {
                    item {
                        VotingCard(gameState, uiState, viewModel)
                    }
                }
                GamePhase.CHAMELEON_GUESS -> {
                    item {
                        ChameleonGuessCard(gameState, uiState, viewModel)
                    }
                }
                GamePhase.ROUND_END -> {
                    item {
                        RoundEndCard(gameState, viewModel)
                    }
                }
                GamePhase.GAME_END -> {
                    item {
                        GameEndCard(gameState)
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun GameInfoCard(
    gameState: com.chameleon.game.GameState,
    uiState: com.chameleon.android.ui.viewmodel.NetworkGameUiState,
    viewModel: NetworkGameViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = gameState.topicCard?.title ?: "Loading...",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            val secretWord = uiState.currentPlayerId?.let { playerId ->
                viewModel.getSecretWordForPlayer(playerId)
            }
            
            if (secretWord != null) {
                Text(
                    text = "Secret Word: $secretWord",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "You are the CHAMELEON! 🦎",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (uiState.isHost) {
                Text(
                    text = "You are hosting this game",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun PlayersCard(
    gameState: com.chameleon.game.GameState,
    uiState: com.chameleon.android.ui.viewmodel.NetworkGameUiState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Players",
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
                            containerColor = if (player.id == uiState.currentPlayerId) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = player.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (player.id == uiState.currentPlayerId) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = "Score: ${player.score}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClueGivingCard(
    gameState: com.chameleon.game.GameState,
    uiState: com.chameleon.android.ui.viewmodel.NetworkGameUiState,
    viewModel: NetworkGameViewModel
) {
    var clue by remember { mutableStateOf("") }
    val currentPlayer = gameState.currentPlayer
    val isMyTurn = currentPlayer?.id == uiState.currentPlayerId
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = if (isMyTurn) "Your turn to give a clue!" else "Waiting for ${currentPlayer?.name ?: "player"}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (isMyTurn) {
                OutlinedTextField(
                    value = clue,
                    onValueChange = { clue = it },
                    label = { Text("Your clue") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true
                )
                
                Button(
                    onClick = {
                        viewModel.submitClue(clue)
                        clue = ""
                    },
                    enabled = clue.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Submit Clue")
                }
            }
            
            if (gameState.clues.isNotEmpty()) {
                Text(
                    text = "Clues given:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                
                gameState.clues.forEach { (playerId, playerClue) ->
                    val player = gameState.players.find { it.id == playerId }
                    Text(
                        text = "${player?.name}: $playerClue",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DiscussionCard(
    gameState: com.chameleon.game.GameState,
    viewModel: NetworkGameViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Discussion Time",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "Discuss the clues and try to identify the Chameleon!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Button(
                onClick = { viewModel.startVoting() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Voting")
            }
        }
    }
}

@Composable
private fun VotingCard(
    gameState: com.chameleon.game.GameState,
    uiState: com.chameleon.android.ui.viewmodel.NetworkGameUiState,
    viewModel: NetworkGameViewModel
) {
    val hasVoted = uiState.currentPlayerId?.let { gameState.votes.containsKey(it) } ?: false
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Vote for the Chameleon",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (hasVoted) {
                Text(
                    text = "You have voted. Waiting for other players...",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                LazyColumn {
                    items(gameState.players.filter { it.id != uiState.currentPlayerId }) { player ->
                        @OptIn(ExperimentalMaterial3Api::class)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            onClick = {
                                viewModel.submitVote(player.id)
                            }
                        ) {
                            Text(
                                text = player.name,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
            
            if (gameState.votes.isNotEmpty()) {
                Text(
                    text = "Votes cast: ${gameState.votes.size}/${gameState.players.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ChameleonGuessCard(
    gameState: com.chameleon.game.GameState,
    uiState: com.chameleon.android.ui.viewmodel.NetworkGameUiState,
    viewModel: NetworkGameViewModel
) {
    var guess by remember { mutableStateOf("") }
    val isChameleon = gameState.chameleonPlayerId == uiState.currentPlayerId
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (isChameleon) {
                Text(
                    text = "You are the Chameleon! Guess the secret word:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = guess,
                    onValueChange = { guess = it },
                    label = { Text("Your guess") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true
                )
                
                Button(
                    onClick = {
                        viewModel.submitChameleonGuess(guess)
                        guess = ""
                    },
                    enabled = guess.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Submit Guess")
                }
            } else {
                Text(
                    text = "Waiting for the Chameleon to guess...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun RoundEndCard(
    gameState: com.chameleon.game.GameState,
    viewModel: NetworkGameViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Round ${gameState.currentRound} Complete!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "The Chameleon was: ${gameState.chameleonPlayer?.name ?: "Unknown"}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Next Round")
            }
        }
    }
}

@Composable
private fun GameEndCard(
    gameState: com.chameleon.game.GameState
) {
    val winner = gameState.players.maxByOrNull { it.score }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Game Over!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "Winner: ${winner?.name ?: "Unknown"}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "Final Scores:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            gameState.players.sortedByDescending { it.score }.forEach { player ->
                Text(
                    text = "${player.name}: ${player.score}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
