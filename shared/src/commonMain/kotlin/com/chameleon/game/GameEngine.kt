package com.chameleon.game

import kotlin.random.Random

class GameEngine {
    private var _gameState = GameState()
    val gameState: GameState get() = _gameState
    
    private val topicCards = TopicCard.createSampleCards()
    
    fun startNewGame(playerNames: List<String>): GameState {
        val players = playerNames.mapIndexed { index, name ->
            Player(id = "player_$index", name = name)
        }
        
        _gameState = GameState(
            players = players,
            phase = GamePhase.SETUP
        )
        
        startNewRound()
        return _gameState
    }
    
    fun startNewRound() {
        // Select random topic card and coordinates
        val topicCard = topicCards.random()
        val row = Random.nextInt(4)
        val col = Random.nextInt(4)
        val secretWord = topicCard.getWordAt(row, col) ?: "Unknown"
        
        // Select random chameleon
        val chameleonIndex = Random.nextInt(_gameState.players.size)
        val chameleonPlayerId = _gameState.players[chameleonIndex].id
        
        // Update players with chameleon status
        val updatedPlayers = _gameState.players.mapIndexed { index, player ->
            player.copy(isChameleon = index == chameleonIndex)
        }
        
        _gameState = _gameState.copy(
            players = updatedPlayers,
            topicCard = topicCard,
            secretWord = secretWord,
            chameleonPlayerId = chameleonPlayerId,
            phase = GamePhase.GIVING_CLUES,
            currentPlayerIndex = 0,
            clues = emptyMap(),
            votes = emptyMap()
        )
    }
    
    fun submitClue(playerId: String, clue: String): GameState {
        val updatedClues = _gameState.clues + (playerId to clue)
        _gameState = _gameState.copy(clues = updatedClues)
        
        // Move to next player or discussion phase
        if (updatedClues.size == _gameState.players.size) {
            _gameState = _gameState.copy(phase = GamePhase.DISCUSSION)
        } else {
            val nextPlayerIndex = (_gameState.currentPlayerIndex + 1) % _gameState.players.size
            _gameState = _gameState.copy(currentPlayerIndex = nextPlayerIndex)
        }
        
        return _gameState
    }
    
    fun startVoting(): GameState {
        _gameState = _gameState.copy(phase = GamePhase.VOTING)
        return _gameState
    }
    
    fun submitVote(voterPlayerId: String, votedPlayerId: String): GameState {
        val updatedVotes = _gameState.votes + (voterPlayerId to votedPlayerId)
        _gameState = _gameState.copy(votes = updatedVotes)
        
        // Check if all players have voted
        if (updatedVotes.size == _gameState.players.size) {
            processVotingResults()
        }
        
        return _gameState
    }
    
    private fun processVotingResults() {
        // Count votes
        val voteCounts = _gameState.votes.values.groupingBy { it }.eachCount()
        val mostVotedPlayerId = voteCounts.maxByOrNull { it.value }?.key
        
        if (mostVotedPlayerId == _gameState.chameleonPlayerId) {
            // Chameleon was caught, give them a chance to guess
            _gameState = _gameState.copy(phase = GamePhase.CHAMELEON_GUESS)
        } else {
            // Wrong player was accused, chameleon wins
            awardPointsToChameleon()
            endRound()
        }
    }
    
    fun submitChameleonGuess(guess: String): GameState {
        if (guess.equals(_gameState.secretWord, ignoreCase = true)) {
            // Chameleon guessed correctly and wins
            awardPointsToChameleon()
        } else {
            // Chameleon guessed wrong, other players win
            awardPointsToOtherPlayers()
        }
        
        endRound()
        return _gameState
    }
    
    private fun awardPointsToChameleon() {
        val updatedPlayers = _gameState.players.map { player ->
            if (player.id == _gameState.chameleonPlayerId) {
                player.copy(score = player.score + 2)
            } else {
                player
            }
        }
        _gameState = _gameState.copy(players = updatedPlayers)
    }
    
    private fun awardPointsToOtherPlayers() {
        val updatedPlayers = _gameState.players.map { player ->
            if (player.id != _gameState.chameleonPlayerId) {
                player.copy(score = player.score + 1)
            } else {
                player
            }
        }
        _gameState = _gameState.copy(players = updatedPlayers)
    }
    
    private fun endRound() {
        if (_gameState.isGameOver) {
            _gameState = _gameState.copy(phase = GamePhase.GAME_END)
        } else {
            _gameState = _gameState.copy(
                phase = GamePhase.ROUND_END,
                currentRound = _gameState.currentRound + 1
            )
        }
    }
    
    fun getSecretWordForPlayer(playerId: String): String? {
        return if (playerId == _gameState.chameleonPlayerId) {
            null // Chameleon doesn't know the secret word
        } else {
            _gameState.secretWord
        }
    }
    
    fun getWinner(): Player? {
        return _gameState.players.maxByOrNull { it.score }?.takeIf { _gameState.isGameOver }
    }
}
