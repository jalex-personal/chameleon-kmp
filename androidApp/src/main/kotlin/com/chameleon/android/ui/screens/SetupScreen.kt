package com.chameleon.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    onStartGame: (List<String>) -> Unit
) {
    var playerNames by remember { mutableStateOf(mutableListOf("Player 1", "Player 2", "Player 3")) }
    var newPlayerName by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "The Chameleon",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Players (${playerNames.size})",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.height(200.dp)
                ) {
                    itemsIndexed(playerNames) { index, name ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { newName ->
                                    playerNames[index] = newName
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            
                            if (playerNames.size > 3) {
                                IconButton(
                                    onClick = { playerNames.removeAt(index) }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove player")
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newPlayerName,
                        onValueChange = { newPlayerName = it },
                        placeholder = { Text("New player name") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    IconButton(
                        onClick = {
                            if (newPlayerName.isNotBlank() && playerNames.size < 8) {
                                playerNames.add(newPlayerName)
                                newPlayerName = ""
                            }
                        },
                        enabled = newPlayerName.isNotBlank() && playerNames.size < 8
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add player")
                    }
                }
                
                Text(
                    text = "3-8 players required",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { onStartGame(playerNames.toList()) },
            enabled = playerNames.size >= 3 && playerNames.all { it.isNotBlank() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Start Game",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "How to Play",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "• One player is secretly the Chameleon\n" +
                            "• Everyone else knows the secret word\n" +
                            "• Give one-word clues related to the topic\n" +
                            "• Vote to identify the Chameleon\n" +
                            "• First to 5 points wins!",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
